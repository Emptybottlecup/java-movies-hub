package ru.practicum.moviehub.http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;
import java.time.Year;

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
                break;
            }
            case "POST": {
                processPostMethod(exchange);
                break;
            }
            case "DELETE": {
                processDeleteMethod(exchange);
                break;
            }
            default: {
                sendJson(exchange, 405, gson.toJson(new ErrorResponse("Method Not Allowed",
                        "Переданный метод невалиден;")));
            }
        }
    }

    private void processGetMethod(HttpExchange ex) throws IOException {
        String[] path = ex.getRequestURI().toString().split("/");
        if (path.length == 2) {
            String[] parametrs = path[1].split("\\?");
            if (parametrs.length == 1) {
                sendJson(ex, 200, gson.toJson(moviesStore.getAllMovies()));
            } else {
                parametrs = parametrs[1].split("=");
                if (parametrs.length == 2 && parametrs[0].equals("year")) {
                    try {
                        int year = Integer.parseInt(parametrs[1]);
                        sendJson(ex, 200, gson.toJson(moviesStore.getMoviesByYear(year)));
                    } catch (NumberFormatException e) {
                        sendJson(ex, 400, gson.toJson(new ErrorResponse("Невалидный запрос",
                                "Значение параметра запроса year должно быть целое число;")));
                    }
                } else {
                    sendJson(ex, 400, gson.toJson(new ErrorResponse("Невалидный запрос",
                            "Некорректный параметр запроса — 'year' или неправильная структура параметра;")));
                }
            }
        } else if (path.length == 3) {
            try {
                int id = Integer.parseInt(path[2]);
                if (moviesStore.containsID(id)) {
                    sendJson(ex, 200, gson.toJson(moviesStore.getMovie(id)));
                } else {
                    sendJson(ex, 404, gson.toJson(new ErrorResponse("Отсутствующий ID",
                            "Фильм не найден;")));
                }
            } catch (NumberFormatException e) {
                sendJson(ex, 400, gson.toJson(new ErrorResponse("Некорректный ID",
                        "Был передан неверный ID;")));
            }
        } else {
            sendJson(ex, 400, gson.toJson(new ErrorResponse("Неверный запрос",
                    "Ошибка в составлении запроса;")));
        }
    }

    private void processPostMethod(HttpExchange ex) throws IOException {
        InputStream is = ex.getRequestBody();

        String body = new String(is.readAllBytes());

        JsonElement jsonElement = JsonParser.parseString(body);

        Headers headers = ex.getRequestHeaders();

        if (!headers.containsKey("Content-Type") || !headers.get("Content-Type").getFirst().equals(CT_JSON)) {
            sendJson(ex, 415, gson.toJson(new ErrorResponse("Неправильное значение заголовка Content-Type",
                    "запрашиваемый тип данных не поддерживается;")));
            return;
        }

        if (!jsonElement.isJsonObject() || jsonElement.getAsJsonObject().get("title") == null || jsonElement
                .getAsJsonObject().get("year") == null) {
            sendJson(ex, 422, gson.toJson(new ErrorResponse("Ошибка валидации", "Неправильно " +
                    "составлено тело запроса;")));
            return;
        }
        int year = jsonElement.getAsJsonObject().get("year").getAsInt();

        String title = jsonElement.getAsJsonObject().get("title").getAsString();

        StringBuilder errors = new StringBuilder();

        if (year < 1888 || Year.now().getValue() + 1 < year) {
            errors.append("Год должен быть между 1888 и 2026;");
        }
        if (title.isEmpty()) {
            errors.append("Название не должно быть пустым;");
        }
        if (title.length() > 100) {
            errors.append("Название слишком длинное;");
        }
        if (!errors.isEmpty()) {
            sendJson(ex, 422, gson.toJson(new ErrorResponse("Ошибка валидации", errors.toString())));
            return;
        }
        sendJson(ex, 201, gson.toJson(moviesStore.addNewMovie(title, year)));
    }

    private void processDeleteMethod(HttpExchange ex) throws IOException {
        String[] path = ex.getRequestURI().toString().split("/");
        if (path.length == 3) {
            try {
                int id = Integer.parseInt(path[2]);
                if (moviesStore.containsID(id)) {
                    moviesStore.deleteMovie(id);
                    sendNoContent(ex);
                } else {
                    sendJson(ex, 404, gson.toJson(new ErrorResponse("Отсутствующий ID",
                            "Фильм не найден;")));
                }
            } catch (NumberFormatException e) {
                sendJson(ex, 400, gson.toJson(new ErrorResponse("Некорректный ID",
                        "Был передан неверный ID;")));
            }
        } else {
            sendJson(ex, 400, gson.toJson(new ErrorResponse("Неверный запрос",
                    "Ошибка в составлении запроса;")));
        }
    }
}
