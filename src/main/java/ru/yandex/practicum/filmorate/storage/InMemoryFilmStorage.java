package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();


    @Override
    public Film create(Film film) {
        log.info("Добавление фильма \"{}\"", film.getName());
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        log.info("Обновление фильма с Id={}", film.getId());
        Film oldFilm = films.get(film.getId());
        oldFilm.setName(film.getName());
        oldFilm.setDescription(film.getDescription());
        oldFilm.setReleaseDate(film.getReleaseDate());
        oldFilm.setDuration(film.getDuration());
        return oldFilm;
    }

    @Override
    public void delete(Integer filmId) {
        log.info("Удаление фильма с id={}", filmId);
        films.remove(filmId);
    }

    @Override
    public Optional<Film> get(Integer filmId) {
        if (!films.containsKey(filmId)) {
            log.info("Фильм с id = {} не найден", filmId);
            return Optional.empty();
        }
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
}
