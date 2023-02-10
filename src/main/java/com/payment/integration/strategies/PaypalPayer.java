package com.payment.integration.strategies;

import com.payment.dto.PaymentRequest;
import com.payment.dto.PaymentType;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.PaymentInstruction;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PaypalPayer implements PaymentSystem {
    private final APIContext apiContext;

    private String helper = "http://localhost:3000/";
    private String successUrl = helper + "success";
    private String cancelUrl = helper + "fail";

    @SneakyThrows
    @Override
    public Payment createPayment(PaymentRequest paymentRequest) {
        Amount amount = new Amount();
        amount.setCurrency(paymentRequest.getCurrency().toString());
        double total = paymentRequest.getAmount().setScale(2, RoundingMode.HALF_UP).doubleValue();
        amount.setTotal(String.format("%.2f", total));

        Transaction transaction = new Transaction();
        transaction.setDescription(paymentRequest.getDescription());
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);
        return payment.create(apiContext);

    }

    @SneakyThrows
    @Override
    public Payment executePayment(String paymentId, String payerId){
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecute = new PaymentExecution();
        paymentExecute.setPayerId(payerId);
        Payment response = payment.execute(apiContext, paymentExecute);
        if(!response.getState().equals("approved")){
            throw new RuntimeException("Not approved");
        }
        return payment;
    }

    @Override
    public PaymentType getName() {
        return PaymentType.PAYPAL;
    }
}
