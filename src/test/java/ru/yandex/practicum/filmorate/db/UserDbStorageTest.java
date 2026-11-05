package ru.yandex.practicum.filmorate.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {
    private final UserDbStorage userStorage;

    @Test
    public void testCreateUser() {
        User user = createUser("1@ya.ru", "user");
        User dbCreatedUser = userStorage.create(user);
        assertNotNull(dbCreatedUser.getId());
        assertEquals(user.getEmail(), dbCreatedUser.getEmail());
        assertEquals(user.getLogin(), dbCreatedUser.getLogin());
    }

    @Test
    public void testUpdateUser() {
        User user = userStorage.create(createUser("1@ya.ru", "user"));
        User updated = createUser(user.getEmail(), user.getLogin());
        updated.setEmail("2@ya.ru");
        updated.setLogin("admin");
        updated.setName("Admin");
        updated.setBirthday(LocalDate.of(2000, 1, 1));
        updated.setId(user.getId());
        User dbUpdatedUser = userStorage.update(updated);

        assertEquals(user.getId(), dbUpdatedUser.getId());
        assertEquals("2@ya.ru", dbUpdatedUser.getEmail());
        assertEquals("admin", dbUpdatedUser.getLogin());
        assertEquals("Admin", dbUpdatedUser.getName());
        assertEquals(LocalDate.of(2000, 1, 1), updated.getBirthday());
    }

    @Test
    public void testAddFriend() {
        User user = userStorage.create(createUser("a@ya.ru", "userA"));
        User friend = userStorage.create(createUser("b@ya.ru", "userB"));
        userStorage.addFriend(user.getId(), friend.getId());

        Collection<User> userFriends = userStorage.getUserFriends(user.getId());

        assertEquals(1, userFriends.size());
        assertTrue(userFriends.stream().anyMatch(u -> u.getId().equals(friend.getId())));
        assertTrue(userStorage.getUserFriends(friend.getId()).isEmpty());
    }

    @Test
    public void testRemoveFriend() {
        User user = userStorage.create(createUser("a@ya.ru", "userA"));
        User friend = userStorage.create(createUser("b@ya.ru", "userB"));

        userStorage.addFriend(user.getId(), friend.getId());
        assertEquals(1, userStorage.getUserFriends(user.getId()).size());

        userStorage.removeFriend(user.getId(), friend.getId());
        assertTrue(userStorage.getUserFriends(user.getId()).isEmpty());
    }

    @Test
    public void testDeleteUser() {
        User user = userStorage.create(createUser("1@ya.ru", "user"));
        assertNotNull(user.getId());
        userStorage.delete(user.getId());
        assertTrue(userStorage.get(user.getId()).isEmpty());
    }

    @Test
    public void testGetUser() {
        User created = userStorage.create(createUser("1@ya.ru", "user"));
        Optional<User> found = userStorage.get(created.getId());
        assertTrue(found.isPresent());

        User user = found.get();

        assertEquals(created.getId(), user.getId());
        assertEquals("1@ya.ru", user.getEmail());
        assertEquals("user", user.getLogin());
        assertEquals(LocalDate.of(1991, 6, 1), user.getBirthday());
    }

    @Test
    public void testGetAllUsers() {
        userStorage.create(createUser("1@ya.ru", "user1"));
        userStorage.create(createUser("2@ya.ru", "user2"));
        userStorage.create(createUser("3@ya.ru", "user3"));

        Collection<User> all = userStorage.getAll();

        assertTrue(all.size() >= 3);
        assertTrue(all.stream().anyMatch(u -> "1@ya.ru".equals(u.getEmail())));
        assertTrue(all.stream().anyMatch(u -> "2@ya.ru".equals(u.getEmail())));
        assertTrue(all.stream().anyMatch(u -> "3@ya.ru".equals(u.getEmail())));
    }

    @Test
    public void testGetUserFriends() {
        User user = userStorage.create(createUser("1@ya.ru", "user"));
        User friend1 = userStorage.create(createUser("2@ya.ru", "friend1"));
        User friend2 = userStorage.create(createUser("3@ya.ru", "friend2"));
        User notFriend = userStorage.create(createUser("4@ya.ru", "notFriend"));

        userStorage.addFriend(user.getId(), friend1.getId());
        userStorage.addFriend(user.getId(), friend2.getId());
        userStorage.addFriend(notFriend.getId(), friend1.getId());

        Collection<User> friends = userStorage.getUserFriends(user.getId());

        assertEquals(2, friends.size());
        assertTrue(friends.stream().anyMatch(u -> u.getId().equals(friend1.getId())));
        assertTrue(friends.stream().anyMatch(u -> u.getId().equals(friend2.getId())));
        assertFalse(friends.stream().anyMatch(u -> u.getId().equals(notFriend.getId())));
    }

    @Test
    public void testGetCommonFriends() {
        User user1 = userStorage.create(createUser("1@ya.ru", "user1"));
        User user2 = userStorage.create(createUser("2@ya.ru", "user2"));
        User common = userStorage.create(createUser("3@ya.ru", "commonFriend"));
        User onlyUser1 = userStorage.create(createUser("cf_uo@ya.ru", "onlyUser1Friend"));
        User onlyUser2 = userStorage.create(createUser("cf_oo@ya.ru", "onlyUser2Friend"));

        userStorage.addFriend(user1.getId(), common.getId());
        userStorage.addFriend(user2.getId(), common.getId());
        userStorage.addFriend(user1.getId(), onlyUser1.getId());
        userStorage.addFriend(user2.getId(), onlyUser2.getId());

        Collection<User> commonFriends = userStorage.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(common.getId(), commonFriends.iterator().next().getId());
    }

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1991, 6, 1));
        return user;
    }
}
