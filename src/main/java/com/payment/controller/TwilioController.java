package com.payment.controller;

import com.payment.dto.ServiceResponse;
import com.payment.dto.TwilioRequest;
import com.payment.dto.TwilioVerificationType;
import com.payment.service.TwilioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("verify")
    @ResponseStatus(code = HttpStatus.OK)
    public ServiceResponse checkOtpCode(@RequestParam(name = "token") String code,
                                        @RequestParam(name = "verificationType") TwilioVerificationType verificationType,
                                        @RequestParam(name = "email", required = false) String email,
                                        @RequestParam(name = "phoneNumber", required = false) String phoneNumber
    ) {
        String receiver = verificationType.equals(TwilioVerificationType.EMAIL) ? email : phoneNumber;
        return twilioService.checkOtp(code, receiver);
    }
}
