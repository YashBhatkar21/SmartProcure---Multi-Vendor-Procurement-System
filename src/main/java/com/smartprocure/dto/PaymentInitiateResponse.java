package com.smartprocure.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentInitiateResponse {
    private String transactionId;
    private Long orderId;
    private String orderNumber;
    private BigDecimal amount;
    private String status;
}
