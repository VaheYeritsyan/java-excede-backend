package com.payment.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TwilioRequest {
    private String email;
    private String otpCode;
    private TwilioVerificationType verificationType;
}
