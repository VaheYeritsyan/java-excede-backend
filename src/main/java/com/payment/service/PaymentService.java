package com.payment.service;

import com.payment.dto.PaymentRequest;
import com.payment.integration.PaymentSystemFactory;
import com.payment.integration.strategies.PaymentSystem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentSystemFactory paymentSystemFactory;

    public Object pay(PaymentRequest paymentRequest) {
        PaymentSystem paymentStrategy = paymentSystemFactory.findPaymentStrategy(paymentRequest.getPaymentType());
        return paymentStrategy.createPayment(paymentRequest);
    }

}
