package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
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

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    @Qualifier("genreDbStorage")
    private final GenreStorage genreStorage;
    @Qualifier("mpaDbStorage")
    private final MpaStorage mpaStorage;

    public Film create(Film film) {
        checkMpa(film);
        checkGenres(film);
        checkFilmDate(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        checkFilmId(film.getId());
        checkMpa(film);
        checkGenres(film);
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

    public Film get(Integer filmId) {
        return filmStorage.get(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
    }

    public void addLike(Integer filmId, Integer userId) {
        checkFilmId(filmId);
        checkUserId(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        checkFilmId(filmId);
        checkUserId(userId);
        filmStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getPopular(Integer count) {
        if (count <= 0) {
            log.error("Значение count должно быть больше нуля");
            throw new ValidationException("Значение count должно быть больше нуля");
        }
        log.info("Получение {} наиболее популярных фильмов", count);
        return filmStorage.getPopular(count);
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

    private void checkMpa(Film film) {
        if (film.getMpa() == null) {
            return;
        }
        if (mpaStorage.get(film.getMpa().getId()).isEmpty()) {
            log.error("Рейтинг MPA с id = {} не найден", film.getMpa().getId());
            throw new NotFoundException("Рейтинг MPA с id = " + film.getMpa().getId() + " не найден");
        }
    }

    private void checkGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        Set<Integer> genreIds = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
        Set<Integer> dbGenreIds = genreStorage.getCollectionByIds(genreIds).stream()
                .map(Genre::getId).collect(Collectors.toSet());
        if (genreIds.size() != dbGenreIds.size()) {
            genreIds.removeAll(dbGenreIds);
            log.error("Жанры с id = {} не найдены", genreIds);
            throw new NotFoundException("Жанры с id = " + genreIds + " не найдены");
        }
    }

    private void checkFilmDate(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            log.error(RELEASE_DATE_ERROR);
            throw new ValidationException(RELEASE_DATE_ERROR);
        }
    }
}
