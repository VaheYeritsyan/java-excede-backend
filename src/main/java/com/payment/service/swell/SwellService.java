package com.payment.service.swell;


import com.payment.dto.TwilioVerificationType;
import com.payment.util.ApiDataObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SwellService {
    private final SwellAccountService swellAccountService;

    public List<TwilioVerificationType> getAvailableVerificationTypes(String email) {
        List<TwilioVerificationType> availableOptions = new ArrayList<>();
        availableOptions.add(TwilioVerificationType.EMAIL);
        log.info("Getting customer {} from swell", email);
        ApiDataObject account = swellAccountService.getAccount(email);
        log.info("Got customer {} from swell", account.get("email"));

        Optional.ofNullable(account.getDataObject("$data").get("phone"))
                .ifPresent(phone -> availableOptions.add(TwilioVerificationType.SMS));
        return availableOptions;
    }
}
