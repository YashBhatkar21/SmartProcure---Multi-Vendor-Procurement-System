package com.smartprocure.controller;

import com.smartprocure.dto.PaymentConfirmRequest;
import com.smartprocure.dto.PaymentDTO;
import com.smartprocure.dto.PaymentInitiateResponse;
import com.smartprocure.entity.User;
import com.smartprocure.repository.UserRepository;
import com.smartprocure.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    public PaymentController(PaymentService paymentService, UserRepository userRepository) {
        this.paymentService = paymentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/initiate/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentInitiateResponse> initiatePayment(
            @PathVariable Long orderId,
            @RequestParam(required = false, defaultValue = "CARD") String method,
            Authentication authentication) {

        User customer = getUserByEmail(authentication.getName());
        return ResponseEntity.ok(paymentService.initiatePayment(orderId, method, customer));
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentDTO> confirmPayment(@Valid @RequestBody PaymentConfirmRequest request) {
        return ResponseEntity.ok(paymentService.confirmPayment(request));
    }

    @GetMapping("/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<PaymentDTO>> getVendorPayments(Authentication authentication) {
        User user = getUserByEmail(authentication.getName());
        return ResponseEntity.ok(paymentService.getVendorPayments(user.getVendor()));
    }

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<PaymentDTO>> getCustomerPayments(Authentication authentication) {
        User user = getUserByEmail(authentication.getName());
        return ResponseEntity.ok(paymentService.getCustomerPayments(user));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
