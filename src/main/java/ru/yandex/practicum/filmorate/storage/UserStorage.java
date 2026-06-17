package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User create(User user);

    User update(User user);

    User delete(Integer userId);

    User get(Integer userId);

    Collection<User> getAll();
}
