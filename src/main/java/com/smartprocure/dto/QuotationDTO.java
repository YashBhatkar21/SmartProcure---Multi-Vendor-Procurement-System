package com.smartprocure.dto;

import com.smartprocure.entity.QuotationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuotationDTO {
    private Long id;
    private Long requestId;
    private Long vendorId;
    private String vendorName;
    private BigDecimal quotedAmount;
    private Instant validUntil;
    private String terms;
    private QuotationStatus status;
    private Instant createdAt;
}
