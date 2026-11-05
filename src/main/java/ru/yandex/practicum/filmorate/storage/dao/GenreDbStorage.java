package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.dao.mappers.GenreRowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository("genreDbStorage")
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbc;
    private final GenreRowMapper mapper;

    @Override
    public Collection<Genre> getAll() {
        log.info("Получение списка жанров");
        return jdbc.query("SELECT * FROM genres ORDER BY id", mapper);
    }

    @Override
    public Collection<Genre> getCollectionByIds(Collection<Integer> genreIds) {
        log.info("Получение списка жанров с id = {}", genreIds);
        if (genreIds == null || genreIds.isEmpty()) {
            return List.of();
        }
        String idList = genreIds.stream().map(String::valueOf)
                .collect(Collectors.joining(","));
        return jdbc.query("SELECT * FROM genres WHERE id IN (" + idList + ") ORDER BY id", mapper);
    }

    @Override
    public Optional<Genre> get(Integer genreId) {
        log.info("Получение жанра с id = {}", genreId);
        return jdbc.query("SELECT * FROM genres WHERE id = ?", mapper, genreId).stream()
                .findFirst();
    }
}
