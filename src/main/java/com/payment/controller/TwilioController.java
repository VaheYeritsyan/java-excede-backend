package com.payment.controller;

import com.payment.dto.ServiceResponse;
import com.payment.dto.TwilioRequest;
import com.payment.service.TwilioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("twilio")
@RestController
@RequiredArgsConstructor
public class TwilioController {
    private final TwilioService twilioService;

    @PostMapping("send-verification")
    @ResponseStatus(code = HttpStatus.OK)
    public ServiceResponse sendVerifyEmail(@RequestBody TwilioRequest twilioRequest) {
        return twilioService.sendVerification(twilioRequest);
    }

    @PostMapping("verify")
    @ResponseStatus(code = HttpStatus.OK)
    public ServiceResponse checkOtpCode(@RequestBody TwilioRequest twilioRequest) {
        return twilioService.checkOtp(twilioRequest);
    }
}
