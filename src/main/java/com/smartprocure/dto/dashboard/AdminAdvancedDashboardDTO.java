package com.smartprocure.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAdvancedDashboardDTO {
    private long totalOrders;
    private long activeVendors;
    private BigDecimal totalRevenue;
    private long totalProcurementRequests;

    // Advanced analytics
    private List<ChartSeriesDTO<BigDecimal>> monthlyRevenue;
    private List<ChartSeriesDTO<Long>> purchasesPerCustomer;
    private List<ChartSeriesDTO<Long>> vendorActivityQuotations;
    private List<ChartSeriesDTO<Long>> vendorActivityOrders;
    private List<ChartSeriesDTO<Long>> orderStatusDistribution;
}
