package com.smartprocure.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalOrders;
    private long activeVendors;
    private BigDecimal totalRevenue;
    private long totalProcurementRequests;
}
