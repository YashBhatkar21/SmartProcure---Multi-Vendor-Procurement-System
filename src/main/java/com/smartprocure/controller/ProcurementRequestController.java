package com.smartprocure.controller;

import com.smartprocure.dto.CreateProcurementRequestRequest;
import com.smartprocure.dto.ProcurementRequestDTO;
import com.smartprocure.entity.RequestStatus;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

        @GetMapping("/me")
        @PreAuthorize("hasRole('CUSTOMER')")
        public ResponseEntity<Page<ProcurementRequestDTO>> getMyRequests(
                        @AuthenticationPrincipal SmartProcurePrincipal principal,
                        @RequestParam(required = false) RequestStatus status,
                        @RequestParam(required = false) String search,
                        Pageable pageable) {
                User customer = userRepository.findByEmail(principal.getUsername())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

                Page<ProcurementRequestDTO> requests = procurementRequestService.getMyRequests(customer, status, search,
                                pageable);
                return ResponseEntity.ok(requests);
        }

        @GetMapping("/available")
        @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
        public ResponseEntity<Page<ProcurementRequestDTO>> getAvailableRequests(
                        @RequestParam(required = false) RequestStatus status,
                        @RequestParam(required = false) String search,
                        Pageable pageable) {
                Page<ProcurementRequestDTO> requests = procurementRequestService.getAvailableRequests(status, search,
                                pageable);
                return ResponseEntity.ok(requests);
        }
}
