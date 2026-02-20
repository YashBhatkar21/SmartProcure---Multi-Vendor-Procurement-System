package com.smartprocure.controller;

import com.smartprocure.dto.CreateProcurementRequestRequest;
import com.smartprocure.dto.ProcurementRequestDTO;
import com.smartprocure.entity.User;
import com.smartprocure.repository.UserRepository;
import com.smartprocure.security.SmartProcurePrincipal;
import com.smartprocure.service.ProcurementRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/procurement-requests")
@RequiredArgsConstructor
public class ProcurementRequestController {

    private final ProcurementRequestService procurementRequestService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ProcurementRequestDTO> createRequest(
            @Valid @RequestBody CreateProcurementRequestRequest request,
            @AuthenticationPrincipal SmartProcurePrincipal principal) {
        User customer = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ProcurementRequestDTO createdRequest = procurementRequestService.createRequest(request, customer);
        return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ProcurementRequestDTO>> getMyRequests(
            @AuthenticationPrincipal SmartProcurePrincipal principal) {
        User customer = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<ProcurementRequestDTO> requests = procurementRequestService.getMyRequests(customer);
        return ResponseEntity.ok(requests);
    }
}
