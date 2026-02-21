package com.smartprocure.controller;

import com.smartprocure.dto.OrderDTO;
import com.smartprocure.entity.OrderStatus;
import com.smartprocure.entity.User;
import com.smartprocure.security.SmartProcurePrincipal;
import com.smartprocure.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final com.smartprocure.repository.UserRepository userRepository;

    private User getUserFromPrincipal(SmartProcurePrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "User not found"));
    }

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<OrderDTO>> getCustomerOrders(
            @AuthenticationPrincipal SmartProcurePrincipal principal,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity
                .ok(orderService.getCustomerOrders(getUserFromPrincipal(principal), status, search, pageable));
    }

    @GetMapping("/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<Page<OrderDTO>> getVendorOrders(
            @AuthenticationPrincipal SmartProcurePrincipal principal,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity
                .ok(orderService.getVendorOrders(getUserFromPrincipal(principal), status, search, pageable));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'VENDOR', 'ADMIN')")
    public ResponseEntity<OrderDTO> getOrderById(
            @PathVariable("orderId") Long orderId,
            @AuthenticationPrincipal SmartProcurePrincipal principal) {
        return ResponseEntity.ok(orderService.getOrderById(orderId, getUserFromPrincipal(principal)));
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('VENDOR', 'CUSTOMER', 'ADMIN')")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable("orderId") Long orderId,
            @RequestParam("status") OrderStatus status,
            @AuthenticationPrincipal SmartProcurePrincipal principal) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status, getUserFromPrincipal(principal)));
    }
}
