package com.payment.service;

import com.payment.dto.PaymentRequest;
import com.payment.dto.PaymentResponse;
import com.payment.dto.PaymentType;
import com.payment.integration.PaymentSystemFactory;
import com.payment.integration.strategies.PaymentSystem;
import com.payment.integration.strategies.impl.PaypalPayer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentSystemFactory paymentSystemFactory;

    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        PaymentSystem paymentSystem = paymentSystemFactory.findPaymentStrategy(paymentRequest.getPaymentType());
        return paymentSystem.createPayment(paymentRequest);
    }

    public PaymentResponse executePayment(String paymentId, String payerId, PaymentType paymentType) {
        if (paymentType != PaymentType.PAYPAL) {
            throw new UnsupportedOperationException("Only PayPal is supported");
        }
        PaypalPayer paymentSystem = (PaypalPayer) paymentSystemFactory.findPaymentStrategy(paymentType);
        return paymentSystem.executePayment(paymentId, payerId);
    }
}
