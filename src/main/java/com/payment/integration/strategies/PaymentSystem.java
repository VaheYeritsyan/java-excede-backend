package com.payment.integration.strategies;

import com.payment.dto.PaymentRequest;
import com.payment.dto.PaymentResponse;
import com.payment.dto.PaymentType;

public interface PaymentSystem {
    PaymentResponse createPayment(PaymentRequest paymentRequest);

    PaymentType getName();
}
