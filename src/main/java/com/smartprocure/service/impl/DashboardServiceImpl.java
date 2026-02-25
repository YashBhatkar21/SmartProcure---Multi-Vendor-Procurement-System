package com.smartprocure.service.impl;

import com.smartprocure.dto.DashboardStatsDTO;
import com.smartprocure.entity.Role;
import com.smartprocure.repository.OrderRepository;
import com.smartprocure.repository.ProcurementRequestRepository;
import com.smartprocure.repository.UserRepository;
import com.smartprocure.repository.QuotationRepository;
import com.smartprocure.repository.PaymentRepository;
import com.smartprocure.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DashboardServiceImpl implements DashboardService {

        private final OrderRepository orderRepository;
        private final UserRepository userRepository;
        private final ProcurementRequestRepository procurementRequestRepository;
        private final QuotationRepository quotationRepository;
        private final PaymentRepository paymentRepository;

        public DashboardServiceImpl(OrderRepository orderRepository, UserRepository userRepository,
                        ProcurementRequestRepository procurementRequestRepository,
                        QuotationRepository quotationRepository, PaymentRepository paymentRepository) {
                this.orderRepository = orderRepository;
                this.userRepository = userRepository;
                this.procurementRequestRepository = procurementRequestRepository;
                this.quotationRepository = quotationRepository;
                this.paymentRepository = paymentRepository;
        }

        @Override
        @Transactional(readOnly = true)
        public DashboardStatsDTO getAdminDashboardStats() {
                long totalOrders = orderRepository.count();

                BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
                if (totalRevenue == null) {
                        totalRevenue = BigDecimal.ZERO;
                }

                long activeVendors = userRepository.countByRole_Name(Role.RoleName.VENDOR);

                long totalRequests = procurementRequestRepository.count();

                return new DashboardStatsDTO(totalOrders, activeVendors, totalRevenue, totalRequests);
        }

        @Override
        @Transactional(readOnly = true)
        public com.smartprocure.dto.dashboard.AdminAdvancedDashboardDTO getAdvancedAdminDashboard() {
                long totalOrders = orderRepository.count();
                BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
                if (totalRevenue == null)
                        totalRevenue = BigDecimal.ZERO;
                long activeVendors = userRepository.countByRole_Name(Role.RoleName.VENDOR);
                long totalRequests = procurementRequestRepository.count();

                java.util.List<com.smartprocure.dto.dashboard.ChartSeriesDTO<BigDecimal>> monthlyRevenue = orderRepository
                                .findMonthlyRevenue();
                java.util.List<com.smartprocure.dto.dashboard.ChartSeriesDTO<Long>> purchasesPerCustomer = orderRepository
                                .findPurchasesPerCustomer();
                java.util.List<com.smartprocure.dto.dashboard.ChartSeriesDTO<Long>> vendorActivityQuotations = quotationRepository
                                .findVendorActivityQuotations();
                java.util.List<com.smartprocure.dto.dashboard.ChartSeriesDTO<Long>> vendorActivityOrders = orderRepository
                                .findVendorActivityOrders();
                java.util.List<com.smartprocure.dto.dashboard.ChartSeriesDTO<Long>> orderStatusDistribution = orderRepository
                                .findOrderStatusDistribution();

                return com.smartprocure.dto.dashboard.AdminAdvancedDashboardDTO.builder()
                                .totalOrders(totalOrders)
                                .activeVendors(activeVendors)
                                .totalRevenue(totalRevenue)
                                .totalProcurementRequests(totalRequests)
                                .monthlyRevenue(monthlyRevenue)
                                .purchasesPerCustomer(purchasesPerCustomer)
                                .vendorActivityQuotations(vendorActivityQuotations)
                                .vendorActivityOrders(vendorActivityOrders)
                                .orderStatusDistribution(orderStatusDistribution)
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public com.smartprocure.dto.dashboard.VendorDashboardStatsDTO getVendorDashboardStats(
                        com.smartprocure.entity.User user) {
                com.smartprocure.entity.Vendor vendor = user.getVendor();
                if (vendor == null) {
                        throw new RuntimeException("User does not have a vendor profile");
                }

                BigDecimal pendingPayments = paymentRepository.sumPendingPaymentsByVendor(vendor);
                if (pendingPayments == null)
                        pendingPayments = BigDecimal.ZERO;

                long ordersCompleted = orderRepository.countByQuotation_VendorAndStatus(vendor,
                                com.smartprocure.entity.OrderStatus.COMPLETED);

                java.util.List<com.smartprocure.dto.dashboard.ChartSeriesDTO<BigDecimal>> monthlyRevenue = orderRepository
                                .findMonthlyRevenueByVendor(vendor);
                BigDecimal totalRevenueGenerated = monthlyRevenue.stream()
                                .map(com.smartprocure.dto.dashboard.ChartSeriesDTO::getValue)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                java.util.List<com.smartprocure.entity.Payment> recentPayments = paymentRepository
                                .findRecentPaymentsByVendor(vendor,
                                                org.springframework.data.domain.PageRequest.of(0, 10));
                java.util.List<com.smartprocure.dto.PaymentDTO> recentPaymentDTOs = recentPayments.stream()
                                .map(p -> com.smartprocure.dto.PaymentDTO.builder()
                                                .id(p.getId())
                                                .amount(p.getAmount())
                                                .paymentMethod(p.getPaymentMethod())
                                                .transactionId(p.getTransactionId())
                                                .status(p.getStatus())
                                                .paidAt(p.getPaidAt())
                                                .orderId(p.getOrder().getId())
                                                .orderNumber(p.getOrder().getOrderNumber())
                                                .build())
                                .collect(java.util.stream.Collectors.toList());

                long totalQuotes = quotationRepository.findByVendor(vendor).size();
                Double performanceScore = 0.0;
                if (totalQuotes > 0) {
                        // Round to 2 decimal places
                        double score = ((double) ordersCompleted / totalQuotes) * 100;
                        performanceScore = Math.round(score * 100.0) / 100.0;
                }

                return com.smartprocure.dto.dashboard.VendorDashboardStatsDTO.builder()
                                .totalRevenueGenerated(totalRevenueGenerated)
                                .ordersCompleted(ordersCompleted)
                                .pendingPayments(pendingPayments)
                                .performanceScore(performanceScore)
                                .monthlyRevenue(monthlyRevenue)
                                .recentPaymentsReceived(recentPaymentDTOs)
                                .build();
        }
}
