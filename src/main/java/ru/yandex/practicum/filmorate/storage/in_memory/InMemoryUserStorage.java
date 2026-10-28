package ru.yandex.practicum.filmorate.storage.in_memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private static final String MISSED_ID_ERROR = "Id должен быть указан";

    @Override
    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь с Id={} успешно создан", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        checkUserId(user.getId());
        User oldUser = users.get(user.getId());
        oldUser.setName(user.isUserNameEmpty() ? user.getLogin() : user.getName());
        oldUser.setLogin(user.getLogin());
        oldUser.setEmail(user.getEmail());
        oldUser.setBirthday(user.getBirthday());
        log.info("Пользователь с Id={} успешно обновлен", oldUser.getId());
        return oldUser;
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        checkUserId(userId);
        checkUserId(friendId);
        User user = users.get(userId);
        User friend = users.get(friendId);
        log.info("Объявление пользователей с id={}, {} друзьями", user.getId(), friend.getId());
        Set<Integer> userFriends = user.getFriends();
        if (userFriends.add(friend.getId())) {
            user.setFriends(userFriends);
            Set<Integer> friendFriends = friend.getFriends();
            friendFriends.add(user.getId());
            friend.setFriends(friendFriends);
        }
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        checkUserId(userId);
        checkUserId(friendId);
        User user = users.get(userId);
        User friend = users.get(friendId);
        log.info("Прекращение дружбы пользователей с id={}, {}", user.getId(), friend.getId());
        Set<Integer> userFriends = user.getFriends();
        if (userFriends.remove(friend.getId())) {
            user.setFriends(userFriends);
            Set<Integer> friendFriends = friend.getFriends();
            friendFriends.remove(user.getId());
            friend.setFriends(friendFriends);
        }
    }

    @Override
    public void delete(Integer userId) {
        checkUserId(userId);
        log.info("Удаление пользователя с id={}", userId);
        users.remove(userId);
    }

    @Override
    public Collection<User> getAll() {
        log.info("Получение списка пользователей");
        return users.values();
    }

    @Override
    public Optional<User> get(Integer userId) {
        if (!users.containsKey(userId)) {
            log.info("Пользователь с id = {} не найден", userId);
            return Optional.empty();
        }
        log.info("Получение пользователя с id = {}", userId);
        return Optional.of(users.get(userId));
    }

    @Override
    public Collection<User> getUserFriends(Integer userId) {
        log.info("Получение списка друзей пользователя с id={}", userId);
        return users.get(userId)
                .getFriends()
                .stream()
                .map(users::get)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<User> getCommonFriends(Integer user1Id, Integer user2Id) {
        if (Objects.equals(user1Id, user2Id)) {
            log.error("Id пользователей совпадают");
            throw new ValidationException("Id пользователей совпадают");
        }
        log.info("Получение списка общих друзей пользователей с id={}, {}", user1Id, user2Id);
        Collection<User> commonFriends = getUserFriends(user1Id);
        commonFriends.retainAll(getUserFriends(user2Id));
        return commonFriends;
    }

    // вспомогательный метод для генерации идентификатора нового пользователя
    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void checkUserId(Integer userId) {
        if (userId == null) {
            log.error(MISSED_ID_ERROR);
            throw new ValidationException(MISSED_ID_ERROR);
        }
        if (!users.containsKey(userId)) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }
}
