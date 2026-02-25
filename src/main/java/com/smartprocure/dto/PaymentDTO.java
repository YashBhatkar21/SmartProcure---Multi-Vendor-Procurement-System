package com.smartprocure.dto;

import com.smartprocure.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PaymentDTO {
    private Long id;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionId;
    private PaymentStatus status;
    private Instant paidAt;
    private Long orderId;
    private String orderNumber;
}
