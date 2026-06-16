package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private static final String MISSED_ID_ERROR = "Id должен быть указан";

    @Override
    public User create(User user) {
        // проверяем выполнение необходимых условий
        user.setId(getNextId());
        if (isUserNameEmpty(user)) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Пользователь с Id={} успешно создан", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        // проверяем необходимые условия
        if (user.getId() == null) {
            log.error(MISSED_ID_ERROR);
            throw new ValidationException(MISSED_ID_ERROR);
        }
        if (users.containsKey(user.getId())) {
            User oldUser = users.get(user.getId());
            oldUser.setName(isUserNameEmpty(user) ? user.getLogin() : user.getName());
            oldUser.setLogin(user.getLogin());
            oldUser.setEmail(user.getEmail());
            oldUser.setBirthday(user.getBirthday());
            log.info("Пользователь с Id={} успешно обновлен", oldUser.getId());
            return oldUser;
        }
        log.error("Пользователь с id = {} не найден", user.getId());
        throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
    }

    @Override
    public User delete(Integer userId) {
        if (!users.containsKey(userId)) {
            log.error(MISSED_ID_ERROR);
            throw new ValidationException(MISSED_ID_ERROR);
        }
        log.info("Удаление пользователя с id={}", userId);
        return users.remove(userId);
    }

    @Override
    public Collection<User> getAll() {
        log.info("Получение списка пользователей");
        return users.values();
    }

    @Override
    public User get(Integer userId) {
        if (!users.containsKey(userId)) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        log.info("Получение пользователя с id = {}", userId);
        return users.get(userId);
    }

    private boolean isUserNameEmpty(User user) {
        return user.getName() == null || user.getName().isBlank();
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
