package com.smartprocure.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentConfirmRequest {

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    // Simulate real gateway confirmation payload
    private boolean isSuccess;
    private String paymentMethod;
}
