package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dao.mappers.FilmRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;

    @Override
    public Film create(Film film) {
        String query = """
                INSERT INTO films (name,  description, release_date, duration) VALUES(?, ?, ?, ?)
                """;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            return ps;
        }, keyHolder);
        Integer id = keyHolder.getKeyAs(Integer.class);
        if (id == null) {
            log.error("Не удалось получить id фильма");
            throw new InternalServerException("Не удалось получить id фильма");
        }
        film.setId(id);
        log.info("Фильм с Id={} успешно создан", film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        String query = """
                UPDATE films
                SET name = ?, description = ?, release_date = ?, duration = ?
                WHERE id = ?
                """;
        int updated = jdbc.update(query,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getId());
        if (updated == 0) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
        Film newFilm = get(film.getId()).orElseThrow();
        log.info("Фильм с Id={} успешно обновлен", newFilm.getId());
        return newFilm;
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        log.info("Пользователь с id={} ставит лайк фильму с id={}", userId, filmId);
        String query = """
                MERGE INTO film_likes (film_id, user_id)
                VALUES (?, ?)
                """;
        jdbc.update(query, filmId, userId);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        log.info("Пользователь с id={} удаляет лайк фильму с id={}", userId, filmId);
        String query = """
                DELETE FROM film_likes
                WHERE film_id = ? AND user_id=  ?
                """;
        jdbc.update(query, filmId, userId);
    }

    @Override
    public void delete(Integer filmId) {
        log.info("Удаление фильма с id={}", filmId);
        jdbc.update("DELETE FROM films WHERE id = ?", filmId);
    }

    @Override
    public Optional<Film> get(Integer filmId) {
        log.info("Получение фильма с id = {}", filmId);
        return jdbc.query("SELECT * FROM films WHERE id = ?", mapper, filmId ).stream()
                .findFirst()
                .map(film -> {
                    film.setLikes(new HashSet<>(
                            jdbc.queryForList("SELECT user_id FROM film_likes WHERE film_id = ?",
                                    Integer.class, filmId)));
                    return film;
                });
    }

    @Override
    public Collection<Film> getAll() {
        log.info("Получение списка фильмов");
        Map<Integer, Set<Integer>> filmLikes = new HashMap<>();
        jdbc.query("SELECT film_id, user_id FROM film_likes", rs -> {
            filmLikes
                    .computeIfAbsent(rs.getInt("film_id"), k -> new HashSet<>())
                    .add(rs.getInt("user_id"));
        });
        List<Film> films = jdbc.query("SELECT * FROM films", mapper);
        for (Film film : films) {
            film.setLikes(filmLikes.getOrDefault(film.getId(), new HashSet<>()));
        }
        return films;
    }
}
