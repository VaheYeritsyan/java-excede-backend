package com.payment.controller;

import com.payment.dto.PaymentRequest;
import com.payment.dto.PaymentType;
import com.payment.integration.strategies.impl.StripePayer;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("stripe")
@RestController
@RequiredArgsConstructor
public class StripeController {
    private final StripePayer stripeClient;

    @PostMapping("/pay")
    @ResponseStatus(code = HttpStatus.OK)
    public Map<String, String> chargeCard(@RequestBody PaymentRequest paymentRequest) {
        paymentRequest.setPaymentType(PaymentType.PAYPAL);
        PaymentIntent payment = (PaymentIntent) stripeClient.createPayment(paymentRequest);
        return Map.of("client_secret", payment.getClientSecret());
    }
}
