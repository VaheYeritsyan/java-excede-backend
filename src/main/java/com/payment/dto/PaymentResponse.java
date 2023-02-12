package com.payment.dto;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PaymentResponse {
    private Object paymentDetails;
    private PaymentResult result;
    private String redirectUrl;
}
