package com.payment.controller;

import com.payment.dto.PaymentRequest;
import com.payment.dto.PaymentResponse;
import com.payment.dto.PaymentType;
import com.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("paypal")
@RestController
@RequiredArgsConstructor
public class PayPalController {
    private final PaymentService paymentService;

    @PostMapping("pay")
    @ResponseStatus(code = HttpStatus.OK)
    public PaymentResponse pay(@RequestBody PaymentRequest paymentRequest) {
        return paymentService.createPayment(paymentRequest);
    }

    @GetMapping("success")
    @ResponseStatus(code = HttpStatus.OK)
    public PaymentResponse executePayment(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
        return paymentService.executePayment(paymentId, payerId, PaymentType.PAYPAL);
    }
}
