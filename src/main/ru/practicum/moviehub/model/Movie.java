package ru.practicum.moviehub.model;

public class Movie {
    private final String title;
    private final int year;
    private final int id;

    public Movie(String title, int year, int id) {
        this.title = title;
        this.year = year;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public int getID() {
        return id;
    }
}