package ru.yandex.practicum.filmorate.storage.in_memory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class InMemoryFilmStorage implements FilmStorage {
    private static final String MISSED_ID_ERROR = "Id должен быть указан";

    private final Map<Integer, Film> films = new HashMap<>();

    @Qualifier("inMemoryUserStorage")
    private final UserStorage userStorage;

    @Override
    public Film create(Film film) {
        log.info("Добавление фильма \"{}\"", film.getName());
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        checkFilmId(film.getId());
        log.info("Обновление фильма с Id={}", film.getId());
        Film oldFilm = films.get(film.getId());
        oldFilm.setName(film.getName());
        oldFilm.setDescription(film.getDescription());
        oldFilm.setReleaseDate(film.getReleaseDate());
        oldFilm.setDuration(film.getDuration());
        return oldFilm;
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        checkFilmId(filmId);
        checkUserId(userId);
        log.info("Пользователь с id={} ставит лайк фильму с id={}", userId, filmId);
        Film film = films.get(filmId);
        Set<Integer> likes = film.getLikes();
        likes.add(userId);
        film.setLikes(likes);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        checkFilmId(filmId);
        checkUserId(userId);
        log.info("Пользователь с id={} удаляет лайк фильму с id={}", userId, filmId);
        Film film = films.get(filmId);
        Set<Integer> likes = film.getLikes();
        likes.remove(userId);
        film.setLikes(likes);
    }

    @Override
    public void delete(Integer filmId) {
        checkFilmId(filmId);
        log.info("Удаление фильма с id={}", filmId);
        films.remove(filmId);
    }

    @Override
    public Optional<Film> get(Integer filmId) {
        checkFilmId(filmId);
        log.info("Получение фильма с id = {}", filmId);
        return Optional.of(films.get(filmId));
    }

    @Override
    public Collection<Film> getAll() {
        log.info("Получение списка фильмов");
        return films.values();
    }

    // вспомогательный метод для генерации идентификатора
    private int getNextId() {
        int currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void checkFilmId(Integer filmId) {
        if (filmId == null) {
            log.error(MISSED_ID_ERROR);
            throw new ValidationException(MISSED_ID_ERROR);
        }
        if (!films.containsKey(filmId)) {
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
}
