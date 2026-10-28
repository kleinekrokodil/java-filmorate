package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    User update(User user);

    void addFriend (Integer userId, Integer friendId);

    void removeFriend (Integer userId, Integer friendId);

    void delete(Integer userId);

    Optional<User> get(Integer userId);

    Collection<User> getAll();

    Collection<User> getUserFriends(Integer userId);

    Collection<User> getCommonFriends(Integer user1Id, Integer user2Id);
}
