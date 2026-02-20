package com.smartprocure.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuotationRequest {

    @NotNull(message = "Request ID is required")
    private Long requestId;

    @NotNull(message = "Quoted amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Quoted amount must be greater than zero")
    private BigDecimal quotedAmount;

    private Instant validUntil;

    private String terms;
}
