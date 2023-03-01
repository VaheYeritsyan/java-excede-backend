package com.payment.exception.classes;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String email) {
        super(String.format("The customer with email %s not found", email));
    }
}
