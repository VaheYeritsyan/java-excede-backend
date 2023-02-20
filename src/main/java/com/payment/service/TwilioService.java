package com.payment.service;

import com.payment.dto.TwilioVerificationType;
import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class TwilioService {

    @Value("${api.twilio.accountSid}")
    private String accountSid;
    @Value("${api.twilio.authToken}")
    private String authToken;
    @Value("${api.twilio.templateSid}")
    private String templateSid;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public String sendVerifyEmail() {
        Verification verification = Verification.creator(templateSid, "eritsyan.01@gmail.com", "email").create();
        log.info("Verification sid is {}", verification.getSid());
        return verification.getSid();
    }

    public String sendVerifySms() {
        Verification verification = Verification.creator(templateSid, "+37494710051", "sms").create();
        log.info("Verification sid is {}", verification.getSid());
        return verification.getSid();
    }

    public void checkOtp(String code, TwilioVerificationType verificationType) {
        String receiver = verificationType.equals(TwilioVerificationType.SMS) ? "+37494710051" : "eritsyan.01@gmail.com";
        VerificationCheck verificationCheck = VerificationCheck.creator(templateSid).setTo(receiver).setCode(code).create();
        log.info("Verification sid is {}", verificationCheck.getSid());
    }
}
