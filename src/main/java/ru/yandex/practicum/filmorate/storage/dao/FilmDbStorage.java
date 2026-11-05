package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.dao.mappers.GenreRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;

@Slf4j
@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;
    private final GenreRowMapper genreMapper;

    @Override
    public Film create(Film film) {
        String query = """
                INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES(?, ?, ?, ?, ?)
                """;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            return ps;
        }, keyHolder);
        Integer id = keyHolder.getKeyAs(Integer.class);
        if (id == null) {
            log.error("Не удалось получить id фильма");
            throw new InternalServerException("Не удалось получить id фильма");
        }
        film.setId(id);
        saveGenres(film);
        log.info("Фильм с Id={} успешно создан", film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        String query = """
                UPDATE films
                SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
                WHERE id = ?
                """;
        int updated = jdbc.update(query,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());
        if (updated == 0) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
        updateGenres(film);
        log.info("Фильм с Id={} успешно обновлен", film.getId());
        return get(film.getId()).orElseThrow();
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
        return jdbc.query("""
                        SELECT f.*, m.name AS mpa_name
                        FROM films f
                        LEFT JOIN mpa m ON m.id = f.mpa_id
                        WHERE f.id = ?
                        """, mapper, filmId).stream()
                .findFirst()
                .map(film -> {
                    film.setLikes(new HashSet<>(
                            jdbc.queryForList("SELECT user_id FROM film_likes WHERE film_id = ?",
                                    Integer.class, filmId)));
                    film.setGenres(new HashSet<>(
                            jdbc.query("""
                                    SELECT g.id, g.name
                                    FROM genres g
                                    JOIN film_genres fg ON fg.genre_id = g.id
                                    WHERE fg.film_id = ?
                                    """, genreMapper, filmId)));
                    return film;
                });
    }

    @Override
    public Collection<Film> getAll() {
        log.info("Получение списка фильмов");
        List<Film> films = jdbc.query("""
                SELECT f.*, m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON m.id = f.mpa_id
                """, mapper);
        loadFilmData(films);
        return films;
    }

    @Override
    public Collection<Film> getPopular(Integer count) {
        log.info("Получение списка {} наиболее популярных фильмов", count);
        List<Film> films = jdbc.query("""
                SELECT f.*, m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON m.id = f.mpa_id
                LEFT JOIN (
                    SELECT film_id, COUNT(*) AS like_count
                    FROM film_likes
                    GROUP BY film_id
                ) lc ON lc.film_id = f.id
                ORDER BY COALESCE(lc.like_count, 0) DESC, f.id ASC
                LIMIT ?
            """, mapper, count);
        loadFilmData(films);
        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        List<Object[]> batch = film.getGenres().stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .toList();
        jdbc.batchUpdate("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", batch);
    }

    private void updateGenres(Film film) {
        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);
    }

    private void loadFilmData(List<Film> films) {
        Map<Integer, Set<Integer>> filmLikes = new HashMap<>();
        jdbc.query("SELECT film_id, user_id FROM film_likes", rs -> {
            filmLikes
                    .computeIfAbsent(rs.getInt("film_id"), k -> new HashSet<>())
                    .add(rs.getInt("user_id"));
        });
        Map<Integer, Set<Genre>> filmGenres = new HashMap<>();
        jdbc.query("""
                SELECT fg.film_id, g.id, g.name
                FROM film_genres fg
                JOIN genres g ON g.id = fg.genre_id
                """, rs -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            filmGenres
                    .computeIfAbsent(rs.getInt("film_id"), k -> new HashSet<>())
                    .add(genre);
        });
        for (Film film : films) {
            film.setLikes(filmLikes.getOrDefault(film.getId(), new HashSet<>()));
            film.setGenres(filmGenres.getOrDefault(film.getId(), new HashSet<>()));
        }
    }
}
