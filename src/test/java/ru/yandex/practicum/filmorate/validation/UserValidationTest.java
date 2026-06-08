package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldValidateCorrectUser() {
        User user = new User();
        user.setEmail("john@john.john");
        user.setLogin("john1");
        user.setName("John");
        user.setBirthday(LocalDate.parse("2000-01-01"));
        assertTrue(validator.validate(user).isEmpty());
    }

    @Test
    void shouldNotValidateEmptyEmail() {
        User user = new User();
        user.setLogin("john1");
        user.setName("John");
        user.setBirthday(LocalDate.parse("2000-01-01"));
        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void shouldNotValidateIncorrectEmail() {
        User user = new User();
        user.setEmail("john.john.john@");
        user.setLogin("john1");
        user.setName("John");
        user.setBirthday(LocalDate.parse("2000-01-01"));
        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void shouldNotValidateFutureBirthdate() {
        User user = new User();
        user.setEmail("john@john.john");
        user.setLogin("john1");
        user.setName("John");
        user.setBirthday(LocalDate.parse("2222-01-01"));
        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void shouldNotValidateEmptyLogin() {
        User user = new User();
        user.setEmail("john@john.john");
        user.setName("John");
        user.setBirthday(LocalDate.parse("2000-01-01"));
        assertFalse(validator.validate(user).isEmpty());
    }

    @Test
    void shouldNotValidateIncorrectLogin() {
        User user = new User();
        user.setEmail("john@john.john");
        user.setLogin("john 1");
        user.setName("John");
        user.setBirthday(LocalDate.parse("2000-01-01"));
        assertFalse(validator.validate(user).isEmpty());
    }
}
