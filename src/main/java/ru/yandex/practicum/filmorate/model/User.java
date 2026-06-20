package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Slf4j
public class User {
    private Integer id;
    @NotBlank(message = "Имейл должен быть указан")
    @Email(message = "Имейл введен некорректно")
    private String email;
    @NotBlank(message = "Логин не должен быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не должен быть пустым или содержать пробелы")
    private String login;
    private String name;
    @NotNull(message = "Дата рождения должна быть заполнена")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
    private Set<Integer> friends = new HashSet<>();

    public boolean isUserNameEmpty() {
        return name == null || name.isBlank();
    }
}
