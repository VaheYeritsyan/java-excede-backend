package com.payment.exception;

import com.payment.exception.error.ApiError;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static com.payment.exception.ResponseErrorsUtil.ERROR_MESSAGES;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        ApiError apiError = buildApiError(BAD_REQUEST, "Validation Error", ex);
        apiError.addFieldErrors(ex.getBindingResult().getFieldErrors());
        apiError.addGlobalErrors(ex.getBindingResult().getGlobalErrors());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler({
            IllegalArgumentException.class
    })
    public ResponseEntity<Object> handleIllegalArgument(RuntimeException ex) {
        return buildResponseEntity(buildApiError(BAD_REQUEST, ex));
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Object> handleArgumentTypeMismatch(RuntimeException ex) {
        String name = ((MethodArgumentTypeMismatchException) ex).getName();
        ApiError apiError = buildApiError(BAD_REQUEST, "Invalid value for query parameter: " + name, ex);
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalState(IllegalStateException ex) {
        return buildResponseEntity(buildApiError(BAD_REQUEST, ex));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        return buildResponseEntity(buildApiError(INTERNAL_SERVER_ERROR, ex.getMessage(), ex));
    }

    private ApiError buildApiError(@Nullable HttpStatus status, String message, Exception ex) {
        if (status == null || status == INTERNAL_SERVER_ERROR) {
            ApiError apiError = ApiError.from(INTERNAL_SERVER_ERROR, ERROR_MESSAGES.get(INTERNAL_SERVER_ERROR).getMessage());
            log.error(String.format("Api error occurred, Id: %s, Message : %s", apiError.getId(), message), ex);
            return apiError;
        }
        return ApiError.from(status, message);
    }

    private ApiError buildApiError(HttpStatus status, Exception ex) {
        log.error("Exception occurred: ", ex);
        return ApiError.from(status, ex.getMessage());
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getHttpStatus());
    }
}
