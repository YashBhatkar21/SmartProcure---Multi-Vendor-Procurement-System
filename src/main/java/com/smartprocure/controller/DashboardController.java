package com.smartprocure.controller;

import com.smartprocure.dto.DashboardStatsDTO;
import com.smartprocure.repository.UserRepository;
import com.smartprocure.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    public DashboardController(DashboardService dashboardService, UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatsDTO> getAdminDashboardStats() {
        return ResponseEntity.ok(dashboardService.getAdminDashboardStats());
    }

    @GetMapping("/admin/advanced")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.smartprocure.dto.dashboard.AdminAdvancedDashboardDTO> getAdvancedAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdvancedAdminDashboard());
    }

    @GetMapping("/vendor/advanced")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<com.smartprocure.dto.dashboard.VendorDashboardStatsDTO> getVendorDashboardStats(
            org.springframework.security.core.Authentication authentication) {
        String email = authentication.getName();
        com.smartprocure.entity.User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(dashboardService.getVendorDashboardStats(user));
    }
}
