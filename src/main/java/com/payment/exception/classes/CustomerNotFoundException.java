package com.payment.exception.classes;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String email) {
        super(String.format("The customer %s not found", email));
    }
}
