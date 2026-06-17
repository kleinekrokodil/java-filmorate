package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

public class FilmValidationTest {
    private FilmController controller;
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @BeforeEach
    void createController() {
        controller = new FilmController(new FilmService(new InMemoryFilmStorage(), new InMemoryUserStorage()));
    }

    @Test
    void shouldCreateValidFilm() {
        Film film = new Film();
        film.setName("Выход рабочих с фабрики");
        film.setDescription("Один из первых фильмов, снятых братьями Люмьер");
        film.setReleaseDate(LocalDate.parse("1895-12-28"));
        film.setDuration(42);
        assertDoesNotThrow(() -> controller.create(film));
        assertTrue(validator.validate(film).isEmpty());
    }

    @Test
    void shouldThrowIfEarlyDate() {
        Film film = new Film();
        film.setName("Выход рабочих с фабрики");
        film.setDescription("Один из первых фильмов, снятых братьями Люмьер");
        film.setReleaseDate(LocalDate.parse("1895-12-27"));
        film.setDuration(42);
        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void shouldNotValidateEmptyName() {
        Film film = new Film();
        film.setDescription("Фильм без названия");
        film.setReleaseDate(LocalDate.parse("2020-01-01"));
        film.setDuration(42);
        assertFalse(validator.validate(film).isEmpty());
    }

    @Test
    void shouldNotValidateLongDescription() {
        Film film = new Film();
        film.setName("Long description film");
        film.setDescription("a".repeat(201));
        film.setReleaseDate(LocalDate.parse("2020-01-01"));
        film.setDuration(42);
        assertFalse(validator.validate(film).isEmpty());
    }

    @Test
    void shouldNotValidateNotPositiveDuration() {
        Film film = new Film();
        film.setName("Negative duration film film");
        film.setDescription("a".repeat(200));
        film.setReleaseDate(LocalDate.parse("2020-01-01"));
        film.setDuration(0);
        assertFalse(validator.validate(film).isEmpty());
        film.setDuration(-1);
        assertFalse(validator.validate(film).isEmpty());
    }
}
