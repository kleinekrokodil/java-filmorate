package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    User update(User user);

    void delete(Integer userId);

    Optional<User> get(Integer userId);

    Collection<User> getAll();
}
