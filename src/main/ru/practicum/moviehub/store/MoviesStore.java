package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MoviesStore {
    private final Map<Integer, Movie> movies;
    private int currentId = 0;

    public MoviesStore() {
        movies = new LinkedHashMap<>();
    }

    public Movie addNewMovie(String title, int year) {
        Movie newMovie = new Movie(title, year, currentId);
        movies.put(currentId++, newMovie);
        return newMovie;
    }

    public List<Movie> getAllMovies() {
        return movies.values().stream().toList();
    }

    public List<Movie> getMoviesByYear(int year) {
        return movies.values().stream().filter(movie -> movie.getYear() == year).toList();
    }

    public void deleteMovie(int id) {
        movies.remove(id);
    }

    public void clear() {
        currentId = 0;
        movies.clear();
    }

    public boolean containsID(int id) {
        return movies.containsKey(id);
    }

    public Movie getMovie(int id) {
        return movies.get(id);
    }
}