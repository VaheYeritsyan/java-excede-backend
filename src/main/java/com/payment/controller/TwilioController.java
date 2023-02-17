package com.payment.controller;

import com.payment.service.TwilioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("twilio")
@RestController
@RequiredArgsConstructor
public class TwilioController {
    private final TwilioService twilioService;

    @PostMapping
    @ResponseStatus(code = HttpStatus.OK)
    public String sendVerifyEmail() {
        return twilioService.sendVerifyEmail();
    }


    @GetMapping("verify")
    @ResponseStatus(code = HttpStatus.OK)
    public void checkOtpCode(@RequestParam(name = "token") String code) {
        twilioService.checkOtp(code);
    }


}
