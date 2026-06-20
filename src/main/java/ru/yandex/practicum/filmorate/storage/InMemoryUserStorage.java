package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь с Id={} успешно создан", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        User oldUser = users.get(user.getId());
        oldUser.setName(user.isUserNameEmpty() ? user.getLogin() : user.getName());
        oldUser.setLogin(user.getLogin());
        oldUser.setEmail(user.getEmail());
        oldUser.setBirthday(user.getBirthday());
        log.info("Пользователь с Id={} успешно обновлен", oldUser.getId());
        return oldUser;
    }

    @Override
    public void delete(Integer userId) {
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

    // вспомогательный метод для генерации идентификатора нового пользователя
    private int getNextId() {
        int currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
