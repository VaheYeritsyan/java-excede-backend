package com.payment.service;

import com.payment.dto.OperationStatus;
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
public class TwilioVerificationService {
    private static final String EMAIL_FIELD = "email";

    @Value("${api.twilio.accountSid}")
    private String accountSid;
    @Value("${api.twilio.authToken}")
    private String authToken;
    @Value("${api.twilio.templateSid}")
    private String templateSid;
    @Value("${frontend.server.url}")
    private String backendUrl;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendVerificationEmail(String email) {
        Verification verification = Verification.creator(templateSid, email, EMAIL_FIELD)
                .setChannelConfiguration(Map.of("substitutions", getCustomTemplateVariables(email)))
                .create();
        log.info("Verification sid is {}", verification.getSid());
    }

    public void sendVerificationSms(String phoneNumber) {
        Verification verification = Verification.creator(templateSid, phoneNumber, "sms").create();
        log.info("Verification sid is {}", verification.getSid());
    }

    public OperationStatus checkOtp(String otpCode, String receiver) {
        VerificationCheck verificationCheck = VerificationCheck.creator(templateSid).setTo(receiver).setCode(otpCode).create();
        String status = verificationCheck.getStatus();
        log.info("Verification sid is {}, and status {}", verificationCheck.getSid(), status);
        return status.equals("approved") ? OperationStatus.APPROVED : OperationStatus.FAILED;

    }

    private Map<String, String> getCustomTemplateVariables(String email) {
        return Map.of("backendUrl", backendUrl, "userEmail", email);
    }
}
