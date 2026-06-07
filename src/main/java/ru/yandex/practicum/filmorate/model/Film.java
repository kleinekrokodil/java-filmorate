package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Data
@Slf4j
public class Film {
    Integer id;
    @NotNull(message = "Название не может быть пустым")
    @NotBlank(message = "Название не может быть пустым")
    String name;
    @Length(max = 200, message = "Максимальная длина описания — 200 символов")
    String description;
    LocalDate releaseDate;
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    Integer duration;
}
