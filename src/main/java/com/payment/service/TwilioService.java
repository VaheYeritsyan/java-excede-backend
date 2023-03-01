package com.payment.service;

import com.payment.dto.OperationStatus;
import com.payment.dto.ServiceResponse;
import com.payment.dto.TwilioRequest;
import com.payment.dto.TwilioVerificationType;
import com.payment.exception.classes.CustomerNotFoundException;
import com.payment.service.swell.SwellAccountService;
import com.payment.util.ApiDataObject;
import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class TwilioService {
    private final SwellAccountService swellAccountService;
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

    public ServiceResponse sendVerification(TwilioRequest twilioRequest) {
        String email = twilioRequest.getEmail();
        if (twilioRequest.getVerificationType() == TwilioVerificationType.EMAIL) {
            return sendVerifyEmail(email);
        }
        String phoneNumber = getUserPhoneNumber(email);
        return sendVerifySms(phoneNumber);
    }

    private String getUserPhoneNumber(String email) {
        log.info("Getting customer {} from swell", email);
        ApiDataObject account = swellAccountService.getAccount(email);
        log.info("Got customer {} from swell", account.get(EMAIL_FIELD));
        ApiDataObject accountDetails = account.getDataObject("$data");
        if (accountDetails == null) {
            throw new CustomerNotFoundException(email);
        }
        return Optional.ofNullable(accountDetails.get("phone"))
                .map(String::valueOf)
                .orElseThrow(() -> new IllegalArgumentException(String.format("The customer %s doesn't have a phone number", accountDetails.get(EMAIL_FIELD))));
    }

    public ServiceResponse sendVerifyEmail(String email) {
        Verification verification = Verification.creator(templateSid, email, EMAIL_FIELD)
                .setChannelConfiguration(Map.of("substitutions", getCustomTemplateVariables(email)))
                .create();
        log.info("Verification sid is {}", verification.getSid());
        return ServiceResponse.builder().operationStatus(OperationStatus.SUCCESSFUL).details(verification).build();
    }

    private Map<String, String> getCustomTemplateVariables(String email) {
        return Map.of("backendUrl", backendUrl, "userEmail", email);
    }

    public ServiceResponse sendVerifySms(String phoneNumber) {
        Verification verification = Verification.creator(templateSid, phoneNumber, "sms").create();
        log.info("Verification sid is {}", verification.getSid());
        return ServiceResponse.builder().operationStatus(OperationStatus.SUCCESSFUL).details(verification).build();
    }

    public ServiceResponse checkOtp(TwilioRequest twilioRequest) {
        String email = twilioRequest.getEmail();
        String receiver = twilioRequest.getVerificationType().equals(TwilioVerificationType.EMAIL) ? email : getUserPhoneNumber(email);
        VerificationCheck verificationCheck = VerificationCheck.creator(templateSid).setTo(receiver).setCode(twilioRequest.getOtpCode()).create();
        String status = verificationCheck.getStatus();
        log.info("Verification sid is {}, and status {}", verificationCheck.getSid(), status);
        OperationStatus operationStatus = status.equals("approved") ? OperationStatus.APPROVED : OperationStatus.FAILED;
        ApiDataObject generatedPassword=null;
        if (operationStatus == OperationStatus.APPROVED) {
            generatedPassword = swellAccountService.generatePasswordToken(email);
        }
        log.info("Generating a password for the user {}", twilioRequest.getEmail());
        return ServiceResponse.builder().operationStatus(operationStatus).details(generatedPassword).build();
    }
}
