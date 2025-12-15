package ru.practicum.moviehub.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;


public class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore moviesStore;
    private final Gson gson;

    public MoviesHandler(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        switch (method) {
            case "GET": {
                processGetMethod(exchange);
            }
            case "POST": {
                processPostMethod(exchange);
            }
            default:
        }
    }

    private void processGetMethod(HttpExchange ex) throws IOException {
        sendJson(ex, 200, "[]");
    }

    private void processPostMethod(HttpExchange ex) throws IOException {
        InputStream is = ex.getRequestBody();

        String body = new String(is.readAllBytes());

        JsonElement jsonElement = JsonParser.parseString(body);


        if(jsonElement.isJsonObject() && jsonElement.getAsJsonObject().get("title") != null && jsonElement
                .getAsJsonObject().get("year") != null) {

            int year = jsonElement.getAsJsonObject().get("year").getAsInt();

            String title = jsonElement.getAsJsonObject().get("title").getAsString();

            String jsonMovie = gson.toJson(moviesStore.addNewMovie(title, year));

            sendJson(ex, 201, jsonMovie);
        }

    }

}
