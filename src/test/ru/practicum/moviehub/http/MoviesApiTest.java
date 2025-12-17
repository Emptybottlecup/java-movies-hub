package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;
    private static final String CT_JSON = "application/json; charset=UTF-8";
    private static final String HEADER = "Content-Type";
    private static final Gson gson = new Gson();
    private static final int SOCKET_NUMBER = 8080;
    private static final String MOVIE_1 = "{\"title\":\"Harry Potter\",\"year\":2001}";
    private static final String MOVIE_2 = "{\"title\":\"Harry Potter 2\",\"year\":2005}";
    private static final String INCORRECT_MOVIE = "{\"title\":\"\",\"year\":2999}";

    @BeforeAll
    static void beforeAll() {
        server = new MoviesServer(new MoviesStore(), SOCKET_NUMBER);

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
        server.stop();
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpResponse<String> resp = getMovieOrMovies("");

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }


    @Test
    void addMovie_withCorrectDetails() throws Exception {
        HttpResponse<String> resp = addMovie(MOVIE_1, HEADER, CT_JSON);

        assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201");
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals(CT_JSON, contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        String body = resp.body().trim();
        assertTrue(body.startsWith("{") && body.endsWith("}"),
                "Ожидается JSON-объект");
        Movie movie = gson.fromJson(body, Movie.class);
        assertEquals("Harry Potter", movie.getTitle());
        assertEquals(2001, movie.getYear());
        assertEquals(0, movie.getID());

        HttpResponse<String> resp2 = addMovie(MOVIE_2,HEADER, CT_JSON);
        String body2 = resp2.body().trim();
        Movie movie2 = gson.fromJson(body2, Movie.class);
        assertEquals("Harry Potter 2", movie2.getTitle());
        assertEquals(2005, movie2.getYear());
        assertEquals(1, movie2.getID());
    }

    @Test
    void addMovie_withIncorrectDetails() throws Exception {
        HttpResponse<String> resp = addMovie(MOVIE_1, "abdsdf", CT_JSON);
        assertEquals(415, resp.statusCode(), "POST /movies должен вернуть 422");
        String body = resp.body().trim();
        assertTrue(body.startsWith("{") && body.endsWith("}"),
                "Ожидается JSON-объект");
        ErrorResponse error = gson.fromJson(body, ErrorResponse.class);
        assertEquals("Неправильное значение заголовка Content-Type", error.getErrorName());
        assertEquals("запрашиваемый тип данных не поддерживается", error.getErrorDetails()[0]);


        HttpResponse<String> resp2 = addMovie(MOVIE_1, HEADER, "Frfrfrfr");
        assertEquals(415, resp2.statusCode(), "POST /movies должен вернуть 422");
        String body2 = resp2.body().trim();
        assertTrue(body.startsWith("{") && body.endsWith("}"),
                "Ожидается JSON-объект");
        ErrorResponse error2 = gson.fromJson(body2, ErrorResponse.class);
        assertEquals("Неправильное значение заголовка Content-Type", error2.getErrorName());
        assertEquals("запрашиваемый тип данных не поддерживается", error2.getErrorDetails()[0]);

        HttpResponse<String> resp3 = addMovie(INCORRECT_MOVIE, HEADER, CT_JSON);
        assertEquals(422, resp3.statusCode(), "POST /movies должен вернуть 422");
        String body3 = resp3.body().trim();
        assertTrue(body3.startsWith("{") && body.endsWith("}"),
                "Ожидается JSON-объект");
        ErrorResponse error3 = gson.fromJson(body3, ErrorResponse.class);
        assertEquals("Ошибка валидации", error3.getErrorName());
        assertEquals("Год должен быть между 1888 и 2026", error3.getErrorDetails()[0]);
        assertEquals("Название не должно быть пустым", error3.getErrorDetails()[1]);
    }

    @Test
    void addAndGetMovies() throws Exception {
        addMovie(MOVIE_1, HEADER, CT_JSON);
        HttpResponse<String> resp1 = getMovieOrMovies("/1");
        String body1 = resp1.body().trim();
        Movie movie1 = gson.fromJson(body1, Movie.class);

        assertEquals(200, resp1.statusCode(), "GET /movies должен вернуть 200");
        assertTrue(body1.startsWith("{") && body1.endsWith("}"),
                "Ожидается JSON-объект");
        assertEquals("Harry Potter", movie1.getTitle());
        assertEquals(2001, movie1.getYear());
        assertEquals(0, movie1.getID());


        addMovie(MOVIE_2, HEADER, CT_JSON);
        HttpResponse<String> resp2 = getMovieOrMovies("/2");
        String body2 = resp2.body().trim();
        Movie movie2 = gson.fromJson(body2, Movie.class);

        assertTrue(body2.startsWith("{") && body2.endsWith("}"),
                "Ожидается JSON-объект");
        assertEquals(200, resp2.statusCode(), "GET /movies должен вернуть 200");
        assertEquals("Harry Potter 2", movie2.getTitle());
        assertEquals(2005, movie2.getYear());
        assertEquals(1, movie2.getID());


        HttpResponse<String> resp3 = getMovieOrMovies("");
        String body3 = resp3.body().trim();
        List<Movie> movies = gson.fromJson(body3, new ListOfMoviesTypeToken().getType());
        assertEquals(200, resp3.statusCode(), "GET /movies должен вернуть 200");
        assertEquals("Harry Potter", movies.getFirst().getTitle());
        assertEquals("Harry Potter 2", movies.getLast().getTitle());
    }

    @Test
    void addAndDeleteMovies() throws Exception {
        addMovie(MOVIE_1, HEADER, CT_JSON);
        addMovie(MOVIE_2, HEADER, CT_JSON);

        HttpResponse<String> resp1 = deleteMovie("/1");
        assertEquals(204, resp1.statusCode());

        HttpResponse<String> resp2 = getMovieOrMovies("");
        String body1 = resp2.body().trim();
        List<Movie> movies = gson.fromJson(body1, new ListOfMoviesTypeToken().getType());
        assertEquals(1, movies.size());
        assertEquals("Harry Potter 2", movies.getFirst().getTitle());

        HttpResponse<String> resp3 = deleteMovie("/3");
        String body2 = resp3.body().trim();
        ErrorResponse error = gson.fromJson(body2,ErrorResponse.class);
        assertEquals(404, resp3.statusCode());
        assertEquals("Фильм не найден" , error.getErrorDetails()[0]);
        assertEquals("Отсутствующий ID", error.getErrorName());



        deleteMovie("/2");
        HttpResponse<String> resp4 = getMovieOrMovies("");
        String body3 = resp4.body().trim();
        List<Movie> movies2 = gson.fromJson(body3, new ListOfMoviesTypeToken().getType());
        assertEquals(0, movies2.size());
    }

    @Test
    void addAndGetMoviesByYear() throws Exception {
        addMovie(MOVIE_1, HEADER, CT_JSON);
        addMovie(MOVIE_2, HEADER, CT_JSON);

        HttpResponse<String> resp1 = getMovieOrMovies("?year=2005");
        String body1 = resp1.body().trim();
        List<Movie> movies = gson.fromJson(body1, new ListOfMoviesTypeToken().getType());
        assertEquals(200, resp1.statusCode());
        assertEquals(1, movies.size());
        assertEquals("Harry Potter 2", movies.getFirst().getTitle());


        HttpResponse<String> resp2 = getMovieOrMovies("?year=2001");
        String body2 = resp2.body().trim();
        List<Movie> movies2 = gson.fromJson(body2, new ListOfMoviesTypeToken().getType());
        assertEquals(200, resp2.statusCode());
        assertEquals(1, movies2.size());
        assertEquals("Harry Potter", movies2.getFirst().getTitle());

        HttpResponse<String> resp3 = getMovieOrMovies("?year=1990");
        String body3 = resp3.body().trim();
        List<Movie> movies3 = gson.fromJson(body3, new ListOfMoviesTypeToken().getType());
        assertEquals(200, resp3.statusCode());
        assertEquals(0, movies3.size());

        HttpResponse<String> resp4 = getMovieOrMovies("?year=199regerg0");
        String body4 = resp4.body().trim();
        ErrorResponse error = gson.fromJson(body4, ErrorResponse.class);
        assertEquals(400, resp4.statusCode());
        assertEquals("Невалидный запрос", error.getErrorName());
        assertEquals("Значение параметра запроса year должно быть целое число", error.getErrorDetails()[0]);

        HttpResponse<String> resp5 = getMovieOrMovies("?yуцацуацуа");
        String body5 = resp5.body().trim();
        ErrorResponse error2 = gson.fromJson(body5, ErrorResponse.class);
        assertEquals(400, resp5.statusCode());
        assertEquals("Невалидный запрос", error2.getErrorName());
        assertEquals("Некорректный параметр запроса — 'year' или неправильная структура параметра", error2.getErrorDetails()[0]);
    }

    private HttpResponse<String> addMovie(String movieJson, String header, String headerValue) throws Exception {
        HttpRequest postReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header(header, headerValue)
                .POST(HttpRequest.BodyPublishers.ofByteArray(movieJson.getBytes(
                        StandardCharsets.UTF_8))).build();
        return client.send(postReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> getMovieOrMovies(String movieId) throws Exception {
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies" + movieId))
                .GET()
                .build();
        return client.send(getReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private HttpResponse<String> deleteMovie(String movieId) throws Exception {
        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies" + movieId))
                .DELETE()
                .build();
        return client.send(getReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}