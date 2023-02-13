package com.payment.integration.strategies.impl;

import com.payment.dto.PaymentRequest;
import com.payment.dto.PaymentResponse;
import com.payment.dto.PaymentResult;
import com.payment.dto.PaymentType;
import com.payment.integration.strategies.PaymentSystem;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PaypalPayer implements PaymentSystem {
    private final APIContext apiContext;

    @Value("${frontend.url}success")
    private String successUrl;
    @Value("${frontend.url}fail")
    private String cancelUrl;

    @SneakyThrows
    @Override
    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        Payment payment = new Payment();
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setRedirectUrls(getRedirectUris(paymentRequest, payment));
        Payment createdPayment = payment.create(apiContext);
        String redirectUri = createdPayment.getLinks()
                .stream()
                .filter(a -> a.getRel().equals("approval_url"))
                .findFirst()
                .map(Links::getHref)
                .orElseThrow(() -> new IllegalStateException("No approval url found in response"));

        return PaymentResponse.builder().paymentDetails(createdPayment).redirectUrl(redirectUri).result(PaymentResult.SUCCESS).build();
    }

    private RedirectUrls getRedirectUris(PaymentRequest paymentRequest, Payment payment) {
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setTransactions(setTransaction(paymentRequest));
        return redirectUrls;
    }

    private static List<Transaction> setTransaction(PaymentRequest paymentRequest) {
        Amount amount = new Amount();
        amount.setCurrency(paymentRequest.getCurrency().toString());
        double total = paymentRequest.getAmount().setScale(2, RoundingMode.HALF_UP).doubleValue();
        amount.setTotal(String.format("%.2f", total));
        Transaction transaction = new Transaction();
        transaction.setDescription(paymentRequest.getDescription());
        transaction.setAmount(amount);
        return List.of(transaction);
    }

    @SneakyThrows
    public PaymentResponse executePayment(String paymentId, String payerId) {
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecute = new PaymentExecution();
        paymentExecute.setPayerId(payerId);
        Payment responsePayment = payment.execute(apiContext, paymentExecute);
        if (!responsePayment.getState().equals("approved")) {
            throw new IllegalStateException(String.format("Payment with id %s was not approved", responsePayment.getId()));
        }
        return PaymentResponse.builder().result(PaymentResult.SUCCESS).paymentDetails(payment).build();
    }

    @Override
    public PaymentType getName() {
        return PaymentType.PAYPAL;
    }
}
