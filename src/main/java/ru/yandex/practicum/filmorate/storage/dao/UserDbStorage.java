package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
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

    private static final String MISSED_ID_ERROR = "Id должен быть указан";

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
        Integer id = keyHolder.getKeyAs(Integer.class);
        if (id == null) {
            log.error("Не удалось получить id пользователя");
            throw new InternalServerException("Не удалось получить id пользователя");
        }
        user.setId(id);
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
        int updated = jdbc.update(query,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());
        if (updated == 0) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }
        log.info("Пользователь с Id={} успешно обновлен", user.getId());
        return  get(user.getId()).orElseThrow();
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        log.info("Объявление пользователя с id={} в друзья пользователя {}", friendId, userId);
        String query = """
                MERGE INTO user_friends (user_id, friend_id) KEY (user_id, friend_id)
                VALUES (?, ?)
                """;
        int updated = jdbc.update(query, userId, friendId);
        if (updated == 0) {
            // Если в наличии оба пользователя - значит они друзья, иначе бросаем исключение
            checkUserId(userId);
            checkUserId(friendId);
        }
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        log.info("Удаление пользователя с id={} из друзей пользователя {}", friendId, userId);
        String query = """
                DELETE FROM user_friends
                WHERE user_id = ? AND friend_id=  ?
                """;
        int removed = jdbc.update(query, userId, friendId);
        if (removed == 0) {
            // Если в наличии оба пользователя - значит они не друзья, иначе бросаем исключение
            checkUserId(userId);
            checkUserId(friendId);
        }
    }

    @Override
    public void delete(Integer userId) {
        log.info("Удаление пользователя с id={}", userId);
        int deleted = jdbc.update("DELETE FROM users WHERE id = ?", userId);
        if (deleted == 0) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
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

    @Override
    public Collection<User> getUserFriends(Integer userId) {
        checkUserId(userId);
        return jdbc.query("""
                        SELECT u.id, u.email, u.login, u.name, u.birthday
                        FROM users u
                        JOIN user_friends uf ON uf.friend_id = u.id
                        WHERE uf.user_id = ?
                        """,
                mapper, userId);
    }

    @Override
    public Collection<User> getCommonFriends(Integer user1Id, Integer user2Id) {
        checkUserId(user1Id);
        checkUserId(user2Id);
        return jdbc.query("""
                        SELECT u.id, u.email, u.login, u.name, u.birthday
                        FROM users u
                        JOIN user_friends uf1 ON uf1.friend_id = u.id
                        JOIN user_friends uf2 ON uf2.friend_id = u.id
                        WHERE uf1.user_id = ? AND uf2.user_id = ?
                        """,
                mapper, user1Id, user2Id);
    }

    private void checkUserId(Integer userId) {
        if (userId == null) {
            log.error(MISSED_ID_ERROR);
            throw new ValidationException(MISSED_ID_ERROR);
        }
        if (get(userId).isEmpty()) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }
}
