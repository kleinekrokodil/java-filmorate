package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);
    private static final String RELEASE_DATE_ERROR = "Дата релиза — не раньше 28 декабря 1895 года;";
    private static final String MISSED_ID_ERROR = "Id должен быть указан";

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film create(Film film) {
        checkFilmDate(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        checkFilmId(film.getId());
        checkFilmDate(film);
        return filmStorage.update(film);
    }

    public void delete(Integer filmId) {
        checkFilmId(filmId);
        filmStorage.delete(filmId);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public void addLike(Integer filmId, Integer userId) {
        checkFilmId(filmId);
        checkUserId(userId);
        Film film = filmStorage.get(filmId).get();
        User user = userStorage.get(userId).get();
        log.info("Пользователь с id={} ставит лайк фильму с id={}", userId, filmId);
        Set<Integer> likes = film.getLikes();
        likes.add(user.getId());
        film.setLikes(likes);
    }

    public void removeLike(Integer filmId, Integer userId) {
        checkFilmId(filmId);
        checkUserId(userId);
        Film film = filmStorage.get(filmId).get();
        User user = userStorage.get(userId).get();
        log.info("Пользователь с id={} удаляет лайк фильму с id={}", userId, filmId);
        Set<Integer> likes = film.getLikes();
        likes.remove(user.getId());
        film.setLikes(likes);
    }

    public Collection<Film> getPopular(Integer count) {
        if (count <= 0) {
            log.error("Значение count должно быть больше нуля");
            throw new ValidationException("Значение count должно быть больше нуля");
        }
        log.info("Получение {} наиболее популярных фильмов", count);
        return filmStorage.getAll().stream()
                .sorted((film1, film2) -> Integer.compare(film2.getLikes().size(), film1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private void checkUserId(Integer userId) {
        if (userId == null) {
            log.error(MISSED_ID_ERROR);
            throw new ValidationException(MISSED_ID_ERROR);
        }
        if (userStorage.get(userId).isEmpty()) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    private void checkFilmId(Integer filmId) {
        if (filmId == null) {
            log.error(MISSED_ID_ERROR);
            throw new ValidationException(MISSED_ID_ERROR);
        }
        if (filmStorage.get(filmId).isEmpty()) {
            log.error("Фильм с id = {} не найден", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
    }

    private void checkFilmDate(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            log.error(RELEASE_DATE_ERROR);
            throw new ValidationException(RELEASE_DATE_ERROR);
        }
    }
}
