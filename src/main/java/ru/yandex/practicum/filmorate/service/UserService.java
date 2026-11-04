package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    private static final String MISSED_ID_ERROR = "Id должен быть указан";

    public User create(User user) {
        if (user.isUserNameEmpty()) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User update(User user) {
        if (user.isUserNameEmpty()) {
            user.setName(user.getLogin());
        }
        return userStorage.update(user);
    }

    public void delete(Integer userId) {
        checkUserId(userId);
        userStorage.delete(userId);
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public User get(Integer userId) {
        return userStorage.get(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
    }

    public void addFriend(Integer userId, Integer friendId) {
        if (Objects.equals(userId, friendId)) {
            log.error("Id пользователей совпадают");
            throw new ValidationException("Id пользователей совпадают");
        }
        checkUserId(userId);
        checkUserId(friendId);
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        if (Objects.equals(userId, friendId)) {
            log.error("Id пользователей совпадают");
            throw new ValidationException("Id пользователей совпадают");
        }
        checkUserId(userId);
        checkUserId(friendId);
        userStorage.removeFriend(userId, friendId);
    }

    public Collection<User> getFriends(Integer userId) {
        checkUserId(userId);
        return userStorage.getUserFriends(userId);
    }

    public Collection<User> getCommonFriends(Integer user1Id, Integer user2Id) {
        if (Objects.equals(user1Id, user2Id)) {
            log.error("Id пользователей совпадают");
            throw new ValidationException("Id пользователей совпадают");
        }
        checkUserId(user1Id);
        checkUserId(user2Id);
        return userStorage.getCommonFriends(user1Id, user2Id);
    }

    private void checkUserId(Integer userId) {
        if (userId == null) {
            log.error(MISSED_ID_ERROR);
            throw new ValidationException(MISSED_ID_ERROR);
        }
        if (userStorage.get(userId).isEmpty()) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }
}
