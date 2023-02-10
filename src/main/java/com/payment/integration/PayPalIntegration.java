//package com.payment.integration;
//
//import com.braintreepayments.http.HttpResponse;
//import com.paypal.core.PayPalHttpClient;
//import com.paypal.orders.AmountWithBreakdown;
//import com.paypal.orders.Order;
//import com.paypal.orders.OrdersCreateRequest;
//import com.paypal.orders.PurchaseUnit;
//import lombok.RequiredArgsConstructor;
//import lombok.SneakyThrows;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.util.Collections;
//
//
//@Component
//@RequiredArgsConstructor
//public class PayPalIntegration implements CommandLineRunner {
//
//
//    @Override
//    public void run(String... args) throws Exception {
//        OrdersCreateRequest request = new OrdersCreateRequest();
//        request.prefer("return=representation");
//        request.requestBody(
//                new Order()
//                        .checkoutPaymentIntent("CAPTURE")
//                        .purchaseUnits(Collections.singletonList(
//                                new PurchaseUnit()
//                                        .amountWithBreakdown(
//                                                new AmountWithBreakdown()
//                                                        .currencyCode("USD")
//                                                        .value("100.00")
//                                        )
//                        ))
//        );
//        HttpResponse<Order> response = paypalClient.execute(request);
//        System.out.println(response);
//    }
//}
//
