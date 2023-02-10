package com.payment.integration;

import com.payment.dto.PaymentType;
import com.payment.integration.strategies.PaymentSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class PaymentSystemFactory {
    private Map<PaymentType, PaymentSystem> strategies;

    @Autowired
    public PaymentSystemFactory(Set<PaymentSystem> paymentSystems) {
        createPaymentSystem(paymentSystems);
    }

    public PaymentSystem findPaymentStrategy(PaymentType paymentType) {
        return strategies.get(paymentType);
    }

    private void createPaymentSystem(Set<PaymentSystem> paymentSystems) {
        strategies = new HashMap<>();
        paymentSystems.forEach(
                paymentSystem -> strategies.put(paymentSystem.getName(), paymentSystem));
    }
}
