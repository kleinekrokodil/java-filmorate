package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.dao.mappers.MpaRowMapper;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository("mpaDbStorage")
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbc;
    private final MpaRowMapper mapper;

    @Override
    public Collection<Mpa> getAll() {
        log.info("Получение списка рейтингов MPA");
        return jdbc.query("SELECT * FROM mpa ORDER BY id", mapper);
    }

    @Override
    public Optional<Mpa> get(Integer mpaId) {
        log.info("Получение рейтинга MPA с id = {}", mpaId);
        return jdbc.query("SELECT * FROM mpa WHERE id = ?", mapper, mpaId).stream()
                .findFirst();
    }
}
