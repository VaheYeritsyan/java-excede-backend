package com.payment.controller;

import com.payment.dto.PaymentRequest;
import com.payment.dto.PaymentResponse;
import com.payment.dto.PaymentType;
import com.payment.integration.strategies.impl.StripePayer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("stripe")
@RestController
@RequiredArgsConstructor
public class StripeController {
    private final StripePayer stripeClient;

    @PostMapping("/pay")
    @ResponseStatus(code = HttpStatus.OK)
    public PaymentResponse chargeCard(@RequestBody PaymentRequest paymentRequest) {
        paymentRequest.setPaymentType(PaymentType.STRIPE);
        return stripeClient.createPayment(paymentRequest);
    }
}
