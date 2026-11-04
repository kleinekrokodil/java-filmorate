package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

public interface GenreStorage {
    Collection<Genre> getAll();

    Collection<Genre> getCollectionByIds(Collection<Integer> genreIds);

    Optional<Genre> get(Integer genreId);
}
