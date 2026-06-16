package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update (Film film) {
        return filmStorage.update(film);
    }

    public Film delete (Integer filmId) {
        return filmStorage.delete(filmId);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }
}
