package com.payment.exception.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.payment.exception.ResponseErrorsUtil.ERROR_MESSAGES;

@Getter
public class ApiError {

    private final UUID id;
    private final int code;
    private final String title;
    private String message = "No message available";

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final LocalDateTime timestamp = LocalDateTime.now();

    private ErrorResponseItem.Type type = ErrorResponseItem.Type.INVALID_REQUEST_ERROR;

    private final Set<ErrorItem> errorItems = new HashSet<>();

    @JsonIgnore
    private final HttpStatus httpStatus;

    public static ApiError from(HttpStatus httpStatus, @Nullable String message) {
        var errorResponseItem = ERROR_MESSAGES.get(httpStatus);
        var apiError = new ApiError(httpStatus);
        apiError.setMessage(message);
        apiError.setType(errorResponseItem.getType());
        return apiError;
    }

    private ApiError(HttpStatus httpStatus) {
        this.id = UUID.randomUUID();
        this.httpStatus = httpStatus;
        this.code = httpStatus.value();
        this.title = httpStatus.getReasonPhrase();
    }

    private void setMessage(String message) {
        if (StringUtils.hasText(message)) {
            this.message = message;
        }
    }

    private void setType(ErrorResponseItem.Type type) {
        this.type = type;
    }

    public void addFieldErrors(List<FieldError> fieldErrors) {
        fieldErrors.stream().map(ErrorItem::from).forEach(errorItems::add);
    }

    public void addGlobalErrors(List<ObjectError> objectErrors) {
        objectErrors.stream().map(ErrorItem::from).forEach(errorItems::add);
    }

}
