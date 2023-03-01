package com.payment.controller;

import com.payment.dto.TwilioVerificationType;
import com.payment.service.swell.SwellService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("swell")
@RequiredArgsConstructor
public class SwellController {
    private final SwellService swellService;

    @GetMapping("verification-options/{email}")
    @ResponseStatus(code = HttpStatus.OK)
    public List<TwilioVerificationType> getAvailableVerificationOptions(@PathVariable(name = "email") String email) {
        return swellService.getAvailableVerificationTypes(email);
    }
}
