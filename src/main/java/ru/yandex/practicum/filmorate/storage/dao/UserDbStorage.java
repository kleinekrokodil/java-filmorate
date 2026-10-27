package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.mappers.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Repository("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;

    @Override
    public User create(User user) {
        String query = """
                INSERT INTO users (email, login, name, birthday) VALUES(?, ?, ?, ?)
                """;
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        user.setId(keyHolder.getKeyAs(Integer.class));
        log.info("Пользователь с Id={} успешно создан", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        String query = """
                UPDATE users
                SET email = ?, login = ?, name = ?, birthday = ?
                WHERE id = ?
                """;
        jdbc.update(query, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        User newUser = get(user.getId()).orElseThrow();
        log.info("Пользователь с Id={} успешно обновлен", newUser.getId());
        return newUser;
    }

    @Override
    public void delete(Integer userId) {
        log.info("Удаление пользователя с id={}", userId);
        jdbc.update("DELETE FROM users WHERE id = ?", userId);
    }

    @Override
    public Optional<User> get(Integer userId) {
        log.info("Получение пользователя с id = {}", userId);
        return jdbc.query("SELECT * FROM users WHERE id = ?", mapper, userId).stream()
                .findFirst()
                .map(user -> {
                    user.setFriends(new HashSet<>(
                        jdbc.queryForList("SELECT friend_id FROM user_friends WHERE user_id = ?",
                            Integer.class, userId)));
                        return user;
                });
    }

    @Override
    public Collection<User> getAll() {
        log.info("Получение списка пользователей");
        Map<Integer, Set<Integer>> friendsByUser = new HashMap<>();
        jdbc.query("SELECT user_id, friend_id FROM user_friends", rs -> {
            friendsByUser
                    .computeIfAbsent(rs.getInt("user_id"), k -> new HashSet<>())
                    .add(rs.getInt("friend_id"));
        });
        List<User> users = jdbc.query("SELECT * FROM users", mapper);
        for (User user : users) {
            user.setFriends(friendsByUser.getOrDefault(user.getId(), new HashSet<>()));
        }
        return users;
    }
}
