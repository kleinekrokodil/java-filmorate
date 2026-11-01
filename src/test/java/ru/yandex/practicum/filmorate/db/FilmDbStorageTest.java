package ru.yandex.practicum.filmorate.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.dao.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class, GenreRowMapper.class,
        UserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Test
    public void testCreateFilm() {
        Film film = createFilm("Тестовый");
        Film dbCreatedFilm = filmStorage.create(film);
        assertNotNull(dbCreatedFilm.getId());
        assertEquals(film.getName(), dbCreatedFilm.getName());
        assertEquals(film.getDuration(), dbCreatedFilm.getDuration());
    }

    @Test
    public void testUpdateFilm() {
        Film film = filmStorage.create(createFilm("Старое имя"));
        Film updated = createFilm("Новое имя");
        updated.setDuration(90);
        updated.setId(film.getId());
        Film dbUpdatedFilm = filmStorage.update(updated);

        assertEquals(film.getId(), dbUpdatedFilm.getId());
        assertEquals("Новое имя", dbUpdatedFilm.getName());
        assertEquals(90, dbUpdatedFilm.getDuration());
    }

    @Test
    public void testAddLike() {
        Film film = filmStorage.create(createFilm("Фильм"));
        User user = userStorage.create(createUser("a@ya.ru", "userA"));

        filmStorage.addLike(film.getId(), user.getId());

        Optional<Film> found = filmStorage.get(film.getId());
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getLikes().size());
        assertTrue(found.get().getLikes().contains(user.getId()));
    }

    @Test
    public void testRemoveLike() {
        Film film = filmStorage.create(createFilm("Фильм"));
        User user = userStorage.create(createUser("a@ya.ru", "userA"));

        filmStorage.addLike(film.getId(), user.getId());
        assertEquals(1, filmStorage.get(film.getId()).orElseThrow().getLikes().size());

        filmStorage.removeLike(film.getId(), user.getId());
        assertTrue(filmStorage.get(film.getId()).orElseThrow().getLikes().isEmpty());
    }

    @Test
    public void testDeleteFilm() {
        Film film = filmStorage.create(createFilm("Удаляемый"));
        assertNotNull(film.getId());
        filmStorage.delete(film.getId());
        assertTrue(filmStorage.get(film.getId()).isEmpty());
    }

    @Test
    public void testGetFilm() {
        Film created = filmStorage.create(createFilm("Тестовый"));
        Optional<Film> found = filmStorage.get(created.getId());
        assertTrue(found.isPresent());

        Film film = found.get();

        assertEquals(created.getId(), film.getId());
        assertEquals("Тестовый", film.getName());
        assertEquals(LocalDate.of(2000, 1, 1), film.getReleaseDate());
        assertEquals(120, film.getDuration());
    }

    @Test
    public void testGetAllFilms() {
        filmStorage.create(createFilm("Фильм 1"));
        filmStorage.create(createFilm("Фильм 2"));
        filmStorage.create(createFilm("Фильм 3"));

        Collection<Film> all = filmStorage.getAll();

        assertTrue(all.size() >= 3);
        assertTrue(all.stream().anyMatch(f -> "Фильм 1".equals(f.getName())));
        assertTrue(all.stream().anyMatch(f -> "Фильм 2".equals(f.getName())));
        assertTrue(all.stream().anyMatch(f -> "Фильм 3".equals(f.getName())));
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
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