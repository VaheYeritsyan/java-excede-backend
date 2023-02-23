package com.payment.service;

import com.payment.dto.OperationStatus;
import com.payment.dto.ServiceResponse;
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

    public ServiceResponse sendVerifyEmail(String email) {
        Verification verification = Verification.creator(templateSid, email, "email").create();
        log.info("Verification sid is {}", verification.getSid());
        return ServiceResponse.builder().operationStatus(OperationStatus.SUCCESSFUL).details(verification).build();
    }

    public ServiceResponse sendVerifySms(String phoneNumber) {
        Verification verification = Verification.creator(templateSid, phoneNumber, "sms").create();
        log.info("Verification sid is {}", verification.getSid());
        return ServiceResponse.builder().operationStatus(OperationStatus.SUCCESSFUL).details(verification).build();
    }

    public ServiceResponse checkOtp(String code, String receiver) {
        VerificationCheck verificationCheck;
        try {
            verificationCheck = VerificationCheck.creator(templateSid).setTo(receiver).setCode(code).create();
        } catch (Exception e) {
            return ServiceResponse.builder().operationStatus(OperationStatus.FAILED).errorMessage(e.getMessage()).build();
        }
        log.info("Verification sid is {}, and status {}", verificationCheck.getSid(), verificationCheck.getStatus());
        OperationStatus operationStatus = verificationCheck.getStatus().equals("approved") ? OperationStatus.APPROVED : OperationStatus.PENDING;
        return ServiceResponse.builder().operationStatus(operationStatus).details(verificationCheck).build();
    }
}
