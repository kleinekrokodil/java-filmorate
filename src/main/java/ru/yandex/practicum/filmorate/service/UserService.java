package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public User delete(Integer userId) {
        return userStorage.delete(userId);
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public void addFriend(Integer userId, Integer friendId) {
        log.info("Объявление пользователей с id={}, {} друзьями", userId, friendId);
        User user = userStorage.get(userId);
        User friend = userStorage.get(friendId);
        Set<Integer> userFriends = user.getFriends();
        if (userFriends.add(friend.getId())) {
            user.setFriends(userFriends);
            Set<Integer> friendFriends = friend.getFriends();
            friendFriends.add(user.getId());
            friend.setFriends(friendFriends);
        }
    }

    public void removeFriend(Integer userId, Integer friendId) {
        log.info("Прекращение дружбы пользователей с id={}, {}", userId, friendId);
        User user = userStorage.get(userId);
        User friend = userStorage.get(friendId);
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
        return userStorage.get(userId).getFriends().stream().map(userStorage::get).collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Integer user1Id, Integer user2Id) {
        log.info("Получение списка общих друзей пользователей с id={}, {}", user1Id, user2Id);
        Collection<User> commonFriends = getFriends(user1Id);
        commonFriends.retainAll(getFriends(user2Id));
        return commonFriends;
    }
}
