package com.payment.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ServiceResponse {
    private OperationStatus operationStatus;
    private Object details;
    private String errorMessage;
}
