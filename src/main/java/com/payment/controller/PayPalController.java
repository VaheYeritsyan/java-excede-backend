package com.payment.controller;

import com.payment.dto.PaymentRequest;
import com.payment.integration.strategies.impl.PaypalPayer;
import com.payment.service.PaymentService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("paypal")
@RestController
@RequiredArgsConstructor
public class PayPalController {
    private final PaymentService paymentService;
    private final PaypalPayer paypalPayer;

    @PostMapping("pay")
    @ResponseStatus(code = HttpStatus.OK)
    public Map<String, String> pay(@RequestBody PaymentRequest paymentRequest) {
        Payment pay = (Payment) paymentService.pay(paymentRequest);
        String redirectUri = pay.getLinks().stream().filter(a -> a.getRel().equals("approval_url")).findFirst().map(Links::getHref).orElseThrow(RuntimeException::new);
        return Map.of("redirectUri", redirectUri);
    }

    @GetMapping("success")
    @ResponseStatus(code = HttpStatus.OK)
    public Payment executePayment(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
        return paypalPayer.executePayment(paymentId, payerId);
    }
}
