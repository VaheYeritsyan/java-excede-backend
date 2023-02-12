package com.payment.exception.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

@Data
@AllArgsConstructor
@NoArgsConstructor
class ErrorItem {
    private String field;
    private String message;
    private Object rejectedValue;


    static ErrorItem from(FieldError fieldError) {
        ErrorItem errorItem = new ErrorItem();
        errorItem.setField(fieldError.getField());
        errorItem.setMessage(fieldError.getDefaultMessage());
        setRejectedValue(errorItem, fieldError.getRejectedValue());
        return errorItem;
    }

    static ErrorItem from(ObjectError objectError) {
        ErrorItem errorItem = new ErrorItem();
        errorItem.setField(objectError.getObjectName());
        errorItem.setMessage(objectError.getDefaultMessage());
        return errorItem;
    }

    private static void setRejectedValue(ErrorItem errorItem, Object rejectedValue) {
        errorItem.setRejectedValue(rejectedValue);
    }
}
