package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;
    private static final int SOCKET_NUMBER = 8080;

    @BeforeAll
    static void beforeAll() throws IOException {
        server = new MoviesServer(new MoviesStore(), SOCKET_NUMBER);

        Gson gson = new Gson();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        server.start();
    }

    @BeforeEach
    void beforeEach() {
        server.clearStorage();
    }

    @AfterAll
    static void afterAll() {
        server.stop(0);
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }


    @Test
    void addMovie_withCorrectDetails() throws Exception {
        Gson gson = new Gson();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofByteArray("{\"title\":\"Harry Potter\",\"year\":2001}".getBytes(
                        StandardCharsets.UTF_8))).build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201");
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();
        assertTrue(body.startsWith("{") && body.endsWith("}"),
                "Ожидается JSON-объект");
        Movie movie = gson.fromJson(body, Movie.class);
        assertEquals("Harry Potter", movie.getTitle());
        assertEquals(2001, movie.getYear());
        assertEquals(0, movie.getID());


        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofByteArray("{\"title\":\"Harry Potter 2\",\"year\":2005}".getBytes(
                        StandardCharsets.UTF_8))).build();
        HttpResponse<String> resp2 =
                client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String body2 = resp2.body().trim();
        Movie movie2 = gson.fromJson(body2, Movie.class);
        assertEquals("Harry Potter 2", movie2.getTitle());
        assertEquals(2005, movie2.getYear());
        assertEquals(1, movie2.getID());
    }
}