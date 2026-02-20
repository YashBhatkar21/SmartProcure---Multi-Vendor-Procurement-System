package com.smartprocure.dto;

import com.smartprocure.entity.RequestStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ProcurementRequestDTO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal budget;
    private Instant dueDate;
    private RequestStatus status;
    private Instant createdAt;
}
