package com.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private PaymentType paymentType;
    private BigDecimal amount;
    private Currency currency;
    private String description;
}
