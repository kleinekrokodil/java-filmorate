package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private static final LocalDate FIRST_FILM_DATE = LocalDate.of(1895, 12, 28);
    private static final String RELEASE_DATE_ERROR = "Дата релиза — не раньше 28 декабря 1895 года;";
    private static final String MISSED_ID_ERROR = "Id должен быть указан";

    @Override
    public Film create(Film film) {
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
            log.error(RELEASE_DATE_ERROR);
            throw new ValidationException(RELEASE_DATE_ERROR);
        }
        log.info("Добавление фильма \"{}\"", film.getName());
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getId() == null) {
            log.error(MISSED_ID_ERROR);
            throw new ValidationException(MISSED_ID_ERROR);
        }
        if (film.getReleaseDate().isBefore(FIRST_FILM_DATE)) {
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

    @Override
    public Film delete(Integer filmId) {
        if (!films.containsKey(filmId)) {
            log.error(MISSED_ID_ERROR);
            throw new ValidationException(MISSED_ID_ERROR);
        }
        log.info("Удаление фильма с id={}", filmId);
        return films.remove(filmId);
    }

    @Override
    public Film get(Integer filmId) {
        if (!films.containsKey(filmId)) {
            log.error("Фильм с id = {} не найден", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        log.info("Получение фильма с id = {}", filmId);
        return films.get(filmId);
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
}
