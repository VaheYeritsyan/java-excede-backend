package com.payment.integration.strategies.impl;

import com.payment.dto.PaymentRequest;
import com.payment.dto.PaymentType;
import com.payment.integration.strategies.PaymentSystem;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class StripePayer implements PaymentSystem {
    @Value("${api.stripe.secretKey}")
    private String apiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }

    @SneakyThrows
    @Override
    public Object createPayment(PaymentRequest paymentRequest) {
        double amount = 100 * paymentRequest.getAmount().setScale(2, RoundingMode.HALF_UP).doubleValue();

        PaymentIntentCreateParams createParams = new
                PaymentIntentCreateParams.Builder()
                .setCurrency(paymentRequest.getCurrency().toString())
                .setAmount((long) amount)
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(createParams);
        System.out.println("Payment intent: " + paymentIntent);
        return paymentIntent;
    }

    @Override
    public PaymentType getName() {
        return PaymentType.STRIPE;
    }
}
