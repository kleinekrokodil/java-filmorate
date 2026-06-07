package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private static final String FIRST_FILM_DATE = "1895-12-28";
    private static final String RELEASE_DATE_ERROR = "Дата релиза — не раньше 28 декабря 1895 года;";
    private static final String MISSED_ID_ERROR = "Id должен быть указан";

    @GetMapping
    public Collection<Film> getAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.parse(FIRST_FILM_DATE))) {
            log.error(RELEASE_DATE_ERROR);
            throw new ValidationException(RELEASE_DATE_ERROR);
        }
        log.info("Добавление фильма \"{}\"", film.getDescription());
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (film.getId() == null) {
            log.error(MISSED_ID_ERROR);
            throw new ValidationException(MISSED_ID_ERROR);
        }
        if (film.getReleaseDate().isBefore(LocalDate.parse(FIRST_FILM_DATE))) {
            log.error(RELEASE_DATE_ERROR);
            throw new ValidationException(RELEASE_DATE_ERROR);
        }
        if (films.containsKey(film.getId())) {
            log.info("Обновление фильма с Id={}", film.getId());
            Film oldFilm = films.get(film.getId());
            oldFilm.setName(film.getName());
            oldFilm.setDescription(film.getDescription());
            oldFilm.setReleaseDate(film.getReleaseDate());
            oldFilm.setDuration(film.getDuration());
            return oldFilm;
        }
        log.error("Фильм с Id={} не найден", film.getId());
        throw new NotFoundException("Фильм с Id=" + film.getId() + " не найден");
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
}
