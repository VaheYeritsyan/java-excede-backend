package com.payment.integration.strategies;

import com.payment.dto.PaymentRequest;
import com.payment.dto.PaymentType;

public interface PaymentSystem {
    Object createPayment(PaymentRequest paymentRequest);

    PaymentType getName();
}
