package ru.practicum.moviehub.api;

public class ErrorResponse {
    private final String errorName;
    private final String[] errorDetails;

    public ErrorResponse(String errorName, String errorDetails) {
        this.errorDetails = errorDetails.split(";");
        this.errorName = errorName;
    }

    public String[] getErrorDetails() {
        return errorDetails;
    }

    public String getErrorName() {
        return errorName;
    }
}