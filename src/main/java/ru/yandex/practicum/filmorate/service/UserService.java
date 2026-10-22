package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private static final String MISSED_ID_ERROR = "Id должен быть указан";

    public User create(User user) {
        if (user.isUserNameEmpty()) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User update(User user) {
        Integer userId = user.getId();
        checkUserId(userId);
        return userStorage.update(user);
    }

    public void delete(Integer userId) {
        checkUserId(userId);
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
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        log.info("Объявление пользователей с id={}, {} друзьями", userId, friendId);
        Set<Integer> userFriends = user.getFriends();
        if (userFriends.add(friend.getId())) {
            user.setFriends(userFriends);
            Set<Integer> friendFriends = friend.getFriends();
            friendFriends.add(user.getId());
            friend.setFriends(friendFriends);
        }
    }

    public void removeFriend(Integer userId, Integer friendId) {
        if (Objects.equals(userId, friendId)) {
            log.error("Id пользователей совпадают");
            throw new ValidationException("Id пользователей совпадают");
        }
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        log.info("Прекращение дружбы пользователей с id={}, {}", userId, friendId);
        Set<Integer> userFriends = user.getFriends();
        if (userFriends.remove(friend.getId())) {
            user.setFriends(userFriends);
            Set<Integer> friendFriends = friend.getFriends();
            friendFriends.remove(user.getId());
            friend.setFriends(friendFriends);
        }
    }

    public Collection<User> getFriends(Integer userId) {
        log.info("Получение списка друзей пользователя с id={}", userId);
        return getUserById(userId)
                .getFriends()
                .stream()
                .map(userStorage::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Integer user1Id, Integer user2Id) {
        if (Objects.equals(user1Id, user2Id)) {
            log.error("Id пользователей совпадают");
            throw new ValidationException("Id пользователей совпадают");
        }
        log.info("Получение списка общих друзей пользователей с id={}, {}", user1Id, user2Id);
        Collection<User> commonFriends = getFriends(user1Id);
        commonFriends.retainAll(getFriends(user2Id));
        return commonFriends;
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

    private User getUserById(Integer userId) {
        checkUserId(userId);
        return userStorage.get(userId).get();
    }
}
