package com.payment.exception;

import com.payment.exception.error.ErrorResponseItem;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static java.util.Map.entry;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseErrorsUtil {
    public static final Map<HttpStatus, ErrorResponseItem> ERROR_MESSAGES = Map.ofEntries(
            entry(BAD_REQUEST, ErrorResponseItem.STATUS_400),
            entry(UNAUTHORIZED, ErrorResponseItem.STATUS_401),
            entry(FORBIDDEN, ErrorResponseItem.STATUS_403),
            entry(NOT_FOUND, ErrorResponseItem.STATUS_404),
            entry(TOO_MANY_REQUESTS, ErrorResponseItem.STATUS_429),
            entry(INTERNAL_SERVER_ERROR, ErrorResponseItem.STATUS_500),
            entry(SERVICE_UNAVAILABLE, ErrorResponseItem.STATUS_503),
            entry(GATEWAY_TIMEOUT, ErrorResponseItem.STATUS_504)
    );
}

