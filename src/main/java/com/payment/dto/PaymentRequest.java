package com.payment.dto;

import com.payment.dto.consts.Currency;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private PaymentType paymentType;
    private BigDecimal amount;
    private Currency currency;
    private String description;
    private String paymentToken;
}
