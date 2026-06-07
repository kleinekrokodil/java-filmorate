package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Data
@Slf4j
public class User {
    Integer id;
    @NotEmpty(message = "Имейл должен быть указан")
    @Email(message = "Имейл введен некорректно")
    String email;
    @Pattern(regexp = "^\\S+$", message = "Логин не должен быть пустым или содержать пробелы")
    String login;
    String name;
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    LocalDate birthday;
}
