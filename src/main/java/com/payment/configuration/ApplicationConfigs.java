//package com.payment.configuration;
//
//import com.paypal.core.PayPalEnvironment;
//import com.paypal.core.PayPalHttpClient;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class ApplicationConfigs {
//    @Value("${api.paypal.clientId}")
//    private String clientId;
//    @Value("${api.paypal.clientSecret}")
//    private String clientSecret;
//
//    @Bean
//    PayPalHttpClient getPaypalClient() {
//        return new PayPalHttpClient(new PayPalEnvironment.Sandbox(clientId, clientSecret));
//    }
//}
