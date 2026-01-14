package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.http.handlers.MoviesHandler;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {
    private final HttpServer server;
    private final MoviesStore moviesStore;

    public MoviesServer(MoviesStore moviesStore, int socketNumber) {
        this.moviesStore = moviesStore;

        try {
            server = HttpServer.create(new InetSocketAddress(socketNumber), 0);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать HTTP-сервер", e);
        }

        creatingContext(moviesStore);
    }

    public void creatingContext(MoviesStore moviesStore) {
        server.createContext("/movies", new MoviesHandler(moviesStore));
    }

    public void clearStorage() {
        moviesStore.clear();
    }

    public void start() {
        server.start();
        System.out.println("Сервер запущен");
    }

    public void stop() {
        server.stop(2);
        System.out.println("Сервер остановлен");
    }
}