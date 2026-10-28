package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
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

    public User create(User user) {
        if (user.isUserNameEmpty()) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public void delete(Integer userId) {
        userStorage.delete(userId);
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public void addFriend(Integer userId, Integer friendId) {
        if (Objects.equals(userId, friendId)) {
            log.error("Id пользователей совпадают");
            throw new ValidationException("Id пользователей совпадают");
        }
        try {
            userStorage.addFriend(userId, friendId);
        } catch (DataIntegrityViolationException e) {
            if (userStorage.get(userId).isEmpty()) {
                throw new NotFoundException("Пользователь с id = " + userId + " не найден");
            }
            if (userStorage.get(friendId).isEmpty()) {
                throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
            }
            throw e;
        }

    }

    public void removeFriend(Integer userId, Integer friendId) {
        if (Objects.equals(userId, friendId)) {
            log.error("Id пользователей совпадают");
            throw new ValidationException("Id пользователей совпадают");
        }
        userStorage.removeFriend(userId, friendId);
    }

    public Collection<User> getFriends(Integer userId) {
        return userStorage.getUserFriends(userId);
    }

    public Collection<User> getCommonFriends(Integer user1Id, Integer user2Id) {
        return userStorage.getCommonFriends(user1Id, user2Id);
    }
}
