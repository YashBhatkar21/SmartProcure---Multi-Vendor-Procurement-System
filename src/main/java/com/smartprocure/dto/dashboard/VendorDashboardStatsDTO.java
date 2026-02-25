package com.smartprocure.dto.dashboard;

import com.smartprocure.dto.PaymentDTO;
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
public class VendorDashboardStatsDTO {
    private BigDecimal totalRevenueGenerated;
    private long ordersCompleted;
    private BigDecimal pendingPayments;
    private Double performanceScore; // Optional e.g. percentage of accepted quotations or completed orders

    private List<ChartSeriesDTO<BigDecimal>> monthlyRevenue;
    private List<PaymentDTO> recentPaymentsReceived;
}
