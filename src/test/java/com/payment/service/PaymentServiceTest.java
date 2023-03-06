package com.payment.service;

import com.payment.dto.PaymentRequest;
import com.payment.dto.PaymentResponse;
import com.payment.dto.PaymentResult;
import com.payment.dto.PaymentType;
import com.payment.integration.PaymentSystemFactory;
import com.payment.integration.strategies.impl.PaypalPayer;
import com.payment.integration.strategies.impl.StripePayer;
import com.paypal.base.rest.APIContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {
        PaymentService.class, PaymentSystemFactory.class})
class PaymentServiceTest {
    @Autowired
    private PaymentService paymentService;

    @MockBean
    private APIContext apiContext;
    @SpyBean
    private PaypalPayer paypalPayer;
    @SpyBean
    private StripePayer stripePayer;
    private final String paymentId = "example payment Id";
    private final String payerId = "examplePayerId";

    @Test
    void createPaymentWithPaypal() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentType(PaymentType.PAYPAL);
        PaymentResponse response = getSampleServiceResponse();
        doReturn(response).when(paypalPayer).createPayment(any());
        PaymentResponse serviceResponse = paymentService.createPayment(paymentRequest);
        assertEquals(response, serviceResponse);
        verify(paypalPayer).createPayment(paymentRequest);
    }


    @Test
    void createPaymentWithStripe() {
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentType(PaymentType.STRIPE);
        PaymentResponse response = getSampleServiceResponse();
        doReturn(response).when(stripePayer).createPayment(any());
        PaymentResponse serviceResponse = paymentService.createPayment(paymentRequest);
        assertEquals(response, serviceResponse);
        verify(stripePayer).createPayment(paymentRequest);
    }

    @Test
    void throwExceptionWhenExecutePaymentWithStripe() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> paymentService.executePayment(paymentId, payerId, PaymentType.STRIPE));
    }

    @Test
    void executePaymentWithPayPal() {
        PaymentResponse response = getSampleServiceResponse();
        doReturn(response).when(paypalPayer).executePayment(paymentId, payerId);
        PaymentResponse serviceResponse = paymentService.executePayment(paymentId, payerId, PaymentType.PAYPAL);
        assertEquals(response, serviceResponse);
        verify(paypalPayer).executePayment(paymentId, payerId);
    }

    private static PaymentResponse getSampleServiceResponse() {
        return PaymentResponse.builder().result(PaymentResult.SUCCESS).build();
    }
}
