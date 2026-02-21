package com.smartprocure.dto;

import com.smartprocure.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private Long quotationId;
    private Long requestId;
    private String requestTitle;
    private String vendorName;
    private String customerName;
    private Instant createdAt;
    private Instant updatedAt;
}
