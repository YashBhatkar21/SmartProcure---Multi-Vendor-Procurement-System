package com.smartprocure.controller;

import com.smartprocure.dto.CreateQuotationRequest;
import com.smartprocure.dto.QuotationDTO;
import com.smartprocure.entity.User;
import com.smartprocure.security.SmartProcurePrincipal;
import com.smartprocure.service.QuotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quotations")
@RequiredArgsConstructor
public class QuotationController {

    private final QuotationService quotationService;
    private final com.smartprocure.repository.UserRepository userRepository;

    private User getUserFromPrincipal(SmartProcurePrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "User not found"));
    }

    @PostMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<QuotationDTO> submitQuotation(
            @Valid @RequestBody CreateQuotationRequest request,
            @AuthenticationPrincipal SmartProcurePrincipal principal) {
        User vendorUser = getUserFromPrincipal(principal);
        return ResponseEntity.ok(quotationService.submitQuotation(request, vendorUser));
    }

    @GetMapping("/request/{requestId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<List<QuotationDTO>> getQuotationsForRequest(
            @PathVariable("requestId") Long requestId,
            @AuthenticationPrincipal SmartProcurePrincipal principal) {
        return ResponseEntity.ok(quotationService.getQuotationsForRequest(requestId, getUserFromPrincipal(principal)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<List<QuotationDTO>> getMyQuotations(
            @AuthenticationPrincipal SmartProcurePrincipal principal) {
        return ResponseEntity.ok(quotationService.getMyQuotations(getUserFromPrincipal(principal)));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<QuotationDTO> acceptQuotation(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal SmartProcurePrincipal principal) {
        return ResponseEntity.ok(quotationService.acceptQuotation(id, getUserFromPrincipal(principal)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<QuotationDTO> rejectQuotation(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal SmartProcurePrincipal principal) {
        return ResponseEntity.ok(quotationService.rejectQuotation(id, getUserFromPrincipal(principal)));
    }
}
