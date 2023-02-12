package com.payment.exception.error;

public enum ErrorResponseItem {

    STATUS_400("The request could not be understood by the server due to malformed syntax., The client SHOULD NOT" +
            " repeat the request without modifications.", Type.INVALID_REQUEST_ERROR),

    STATUS_401("The request requires client authentication.", Type.AUTHENTICATION_ERROR),

    STATUS_403("The request requires client authorisation to access resource.",
            Type.AUTHENTICATION_ERROR),

    STATUS_404("The server cannot find the requested resource.",
            Type.INVALID_REQUEST_ERROR),

    STATUS_429("The number of requests from this client is restricted to a specified quota.",
            Type.RATE_LIMIT_ERROR),

    STATUS_500("The server encountered an unexpected condition which prevented it from fulfilling the request.",
            Type.API_ERROR),

    STATUS_503("The server is not ready to handle the request. " +
            "If specified please check the <strong>Retry-After</strong> " +
            "for the time period specified for recovery/re-attempt of request.",
            Type.API_ERROR),

    STATUS_504("The server, while acting as a gateway or proxy," +
            " did not get a response in time from the upstream server that it needed in order to complete the request.",
            Type.API_ERROR);


    private final String message;
    private final Type type;

    ErrorResponseItem(String message, Type type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public Type getType() {
        return type;
    }

    enum Type {
        API_CONNECTION_ERROR,
        API_ERROR,
        AUTHENTICATION_ERROR,
        IDEMPOTENCY_ERROR,
        INVALID_REQUEST_ERROR,
        RATE_LIMIT_ERROR
    }
}
