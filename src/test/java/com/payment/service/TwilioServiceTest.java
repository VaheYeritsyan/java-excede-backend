package com.payment.service;

import com.payment.dto.OperationStatus;
import com.payment.dto.ServiceResponse;
import com.payment.dto.TwilioRequest;
import com.payment.dto.TwilioVerificationType;
import com.payment.service.swell.SwellAccountService;
import com.payment.util.ApiDataObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {TwilioService.class})
class TwilioServiceTest {

    @MockBean
    private SwellAccountService swellAccountService;
    @MockBean
    private TwilioVerificationService twilioVerificationService;
    @Autowired
    private TwilioService twilioService;
    private final String email = "example@gmail.com";
    private final String phone = "+37494710051";

    @Test
    void sendVerificationSms() {
        ServiceResponse response = getSuccessServiceResponse();
        TwilioRequest twilioRequest = new TwilioRequest();
        twilioRequest.setEmail(email);
        twilioRequest.setVerificationType(TwilioVerificationType.SMS);
        mockPhoneNumber();
        ServiceResponse serviceResponse = twilioService.sendVerification(twilioRequest);
        verify(twilioVerificationService).sendVerificationSms(phone);
        Assertions.assertEquals(serviceResponse, response);
    }


    @Test
    void sendVerificationEmail() {
        ServiceResponse response = getSuccessServiceResponse();
        TwilioRequest twilioRequest = new TwilioRequest();
        twilioRequest.setEmail(email);
        twilioRequest.setVerificationType(TwilioVerificationType.EMAIL);
        ServiceResponse serviceResponse = twilioService.sendVerification(twilioRequest);
        verify(twilioVerificationService).sendVerificationEmail(email);
        Assertions.assertEquals(serviceResponse, response);
    }

    @Test
    void checkVerificationOtpWithSms() {
        String passwordToken = "passwordToken";
        ApiDataObject generatePasswordResponse = new ApiDataObject(Map.of(passwordToken, passwordToken));
        TwilioRequest twilioRequest = new TwilioRequest();
        twilioRequest.setEmail(email);
        twilioRequest.setOtpCode("145623");
        twilioRequest.setVerificationType(TwilioVerificationType.SMS);
        mockPhoneNumber();

        when(twilioVerificationService.checkOtp(twilioRequest.getOtpCode(), phone)).thenReturn(OperationStatus.APPROVED);
        when(swellAccountService.generatePasswordToken(email)).thenReturn(generatePasswordResponse);

        ServiceResponse serviceResponse = twilioService.checkOtp(twilioRequest);
        verify(twilioVerificationService).checkOtp(twilioRequest.getOtpCode(), phone);
        verify(swellAccountService).generatePasswordToken(email);

        Assertions.assertEquals(serviceResponse.getDetails(), generatePasswordResponse);
    }

    @Test
    void checkVerificationOtpWithEmail() {
        String passwordToken = "passwordToken";
        ApiDataObject generatePasswordResponse = new ApiDataObject(Map.of(passwordToken, passwordToken));
        TwilioRequest twilioRequest = new TwilioRequest();
        twilioRequest.setEmail(email);
        twilioRequest.setOtpCode("145623");
        twilioRequest.setVerificationType(TwilioVerificationType.EMAIL);

        when(twilioVerificationService.checkOtp(twilioRequest.getOtpCode(), email)).thenReturn(OperationStatus.APPROVED);
        when(swellAccountService.generatePasswordToken(email)).thenReturn(generatePasswordResponse);

        ServiceResponse serviceResponse = twilioService.checkOtp(twilioRequest);
        verify(twilioVerificationService).checkOtp(twilioRequest.getOtpCode(), email);
        verify(swellAccountService).generatePasswordToken(email);
        verify(swellAccountService, never()).getAccount(email);

        Assertions.assertEquals(serviceResponse.getDetails(), generatePasswordResponse);
    }


    private static ServiceResponse getSuccessServiceResponse() {
        return ServiceResponse.builder().operationStatus(OperationStatus.SUCCESSFUL).build();
    }

    private void mockPhoneNumber() {
        ApiDataObject account = new ApiDataObject();
        ApiDataObject accountDetails = new ApiDataObject();
        accountDetails.put("phone", phone);
        account.put("$data", accountDetails);
        when(swellAccountService.getAccount(email)).thenReturn(account);
    }

}
