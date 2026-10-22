package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Slf4j
public class Film {
    private Integer id;
    @NotBlank(message = "Название не может быть пустым")
    private String name;
    @Length(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;
    @NotNull(message = "Дата выхода фильма должна быть заполнена")
    private LocalDate releaseDate;
    @NotNull(message = "Продолжительность фильма должна быть заполнена")
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;
    private Set<Integer> likes = new HashSet<>();
}
