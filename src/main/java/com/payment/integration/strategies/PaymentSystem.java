package com.payment.integration.strategies;

import com.payment.dto.PaymentRequest;
import com.payment.dto.PaymentType;
import com.paypal.api.payments.Payment;

public interface PaymentSystem {
    Payment createPayment(PaymentRequest paymentRequest);

    public Payment executePayment(String paymentId, String payerId);

    PaymentType getName();
}
