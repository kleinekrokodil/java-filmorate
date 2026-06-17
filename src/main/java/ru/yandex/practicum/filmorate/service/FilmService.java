package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public void delete(Integer filmId) {
        filmStorage.delete(filmId);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = filmStorage.get(filmId);
        User user = userStorage.get(userId);
        log.info("Пользователь с id={} ставит лайк фильму с id={}", userId, filmId);
        Set<Integer> likes = film.getLikes();
        likes.add(user.getId());
        film.setLikes(likes);
    }

    public void removeLike(Integer filmId, Integer userId) {
        Film film = filmStorage.get(filmId);
        User user = userStorage.get(userId);
        log.info("Пользователь с id={} удаляет лайк фильму с id={}", userId, filmId);
        Set<Integer> likes = film.getLikes();
        likes.remove(user.getId());
        film.setLikes(likes);
    }

    public Collection<Film> getPopular(Integer count) {
        log.info("Получение {} наиболее популярных фильмов", count);
        return filmStorage.getAll().stream()
                .sorted((film1, film2) -> Integer.compare(film2.getLikes().size(), film1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
