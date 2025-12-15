package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.HashMap;
import java.util.Map;

public class MoviesStore {
    private final Map<Integer, Movie> movies;
    private int currentId = 0;

    public MoviesStore() {
        movies = new HashMap<>();
    }

    public Movie addNewMovie(String title, int year) {
        Movie newMovie = new Movie(title, year, currentId++);
        movies.put(currentId, newMovie);
        return newMovie;
    }

    public void clear() {
        movies.clear();
    }

    public Movie getMovie (int id) {
        return movies.get(id);
    }
}