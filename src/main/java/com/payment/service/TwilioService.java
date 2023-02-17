package com.payment.service;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
@Slf4j
public class TwilioService {

    @Value("${api.twilio.accountSid}")
    private String accountSid;
    @Value("${api.twilio.authToken}")
    private String authToken;
    @Value("${api.twilio.verifyTemplateId}")
    private String templateId;
    @Value("${api.twilio.otpCode}")
    private String otpCode;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public String sendVerifyEmail() {

        Verification verification = Verification.creator(
                        templateId,
                        "eritsyan.01@gmail.com",
                        "email")
                .setChannelConfiguration(Map.of("twilio_code", otpCode))
                .create();
        log.info("Verification sid is {}", verification.getSid());
        return verification.getSid();
    }

    public void checkOtp(String code) {
        VerificationCheck verificationCheck = VerificationCheck.creator(
                        templateId)
                .setTo("eritsyan.01@gmail.com")
                .setCode(code)
                .create();
        log.info("Verification sid is {}", verificationCheck.getSid());

    }
}
