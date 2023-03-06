package com.payment.service;

import com.payment.dto.OperationStatus;
import com.payment.dto.ServiceResponse;
import com.payment.dto.TwilioRequest;
import com.payment.dto.TwilioVerificationType;
import com.payment.exception.classes.CustomerNotFoundException;
import com.payment.service.swell.SwellAccountService;
import com.payment.util.ApiDataObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class TwilioService {
    private final SwellAccountService swellAccountService;
    private final TwilioVerificationService twilioVerificationService;
    private static final String EMAIL_FIELD = "email";

    public ServiceResponse sendVerification(TwilioRequest twilioRequest) {
        String email = twilioRequest.getEmail();
        if (twilioRequest.getVerificationType() == TwilioVerificationType.EMAIL) {
            return sendVerifyEmail(email);
        }
        String phoneNumber = getUserPhoneNumber(email);
        return sendVerifySms(phoneNumber);
    }

    public String getUserPhoneNumber(String email) {
        log.info("Getting customer {} from swell", email);
        ApiDataObject account = swellAccountService.getAccount(email);
        ApiDataObject accountDetails = account.getDataObject("$data");
        if (accountDetails == null) {
            throw new CustomerNotFoundException(email);
        }
        log.info("Got customer {} from swell", accountDetails.get(EMAIL_FIELD));
        return Optional.ofNullable(accountDetails.get("phone"))
                .map(String::valueOf)
                .orElseThrow(() -> new IllegalArgumentException(String.format("The customer %s doesn't have a phone number", accountDetails.get(EMAIL_FIELD))));
    }

    public ServiceResponse sendVerifyEmail(String email) {
        twilioVerificationService.sendVerificationEmail(email);
        return ServiceResponse.builder().operationStatus(OperationStatus.SUCCESSFUL).build();
    }

    public ServiceResponse sendVerifySms(String phoneNumber) {
        twilioVerificationService.sendVerificationSms(phoneNumber);
        return ServiceResponse.builder().operationStatus(OperationStatus.SUCCESSFUL).build();
    }

    public ServiceResponse checkOtp(TwilioRequest twilioRequest) {
        String email = twilioRequest.getEmail();
        String receiver = twilioRequest.getVerificationType().equals(TwilioVerificationType.EMAIL) ? email : getUserPhoneNumber(email);
        OperationStatus operationStatus = twilioVerificationService.checkOtp(twilioRequest.getOtpCode(), receiver);
        ApiDataObject generatedPassword = null;
        if (operationStatus == OperationStatus.APPROVED) {
            generatedPassword = swellAccountService.generatePasswordToken(email);
            log.info("Generating a password for the user {}", twilioRequest.getEmail());
        }
        return ServiceResponse.builder().operationStatus(operationStatus).details(generatedPassword).build();
    }
}
