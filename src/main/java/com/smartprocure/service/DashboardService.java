package com.smartprocure.service;

import com.smartprocure.dto.DashboardStatsDTO;

public interface DashboardService {
    DashboardStatsDTO getAdminDashboardStats();

    com.smartprocure.dto.dashboard.AdminAdvancedDashboardDTO getAdvancedAdminDashboard();

    com.smartprocure.dto.dashboard.VendorDashboardStatsDTO getVendorDashboardStats(com.smartprocure.entity.User user);
}
