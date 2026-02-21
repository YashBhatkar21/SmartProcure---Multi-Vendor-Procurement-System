package com.smartprocure.service.impl;

import com.smartprocure.dto.DashboardStatsDTO;
import com.smartprocure.entity.Role;
import com.smartprocure.repository.OrderRepository;
import com.smartprocure.repository.ProcurementRequestRepository;
import com.smartprocure.repository.UserRepository;
import com.smartprocure.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProcurementRequestRepository procurementRequestRepository;

    public DashboardServiceImpl(OrderRepository orderRepository, UserRepository userRepository,
            ProcurementRequestRepository procurementRequestRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.procurementRequestRepository = procurementRequestRepository;
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
}
