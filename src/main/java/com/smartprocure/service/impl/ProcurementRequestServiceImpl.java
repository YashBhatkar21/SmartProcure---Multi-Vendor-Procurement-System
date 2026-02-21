package com.smartprocure.service.impl;

import com.smartprocure.dto.CreateProcurementRequestRequest;
import com.smartprocure.dto.ProcurementRequestDTO;
import com.smartprocure.entity.ProcurementRequest;
import com.smartprocure.entity.RequestStatus;
import com.smartprocure.entity.User;
import com.smartprocure.entity.Role;
import com.smartprocure.repository.ProcurementRequestRepository;
import com.smartprocure.service.ProcurementRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.smartprocure.specification.ProcurementRequestSpecification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class ProcurementRequestServiceImpl implements ProcurementRequestService {

    private static final Logger log = LoggerFactory.getLogger(ProcurementRequestServiceImpl.class);

    private final ProcurementRequestRepository procurementRequestRepository;

    @Override
    @Transactional
    public ProcurementRequestDTO createRequest(CreateProcurementRequestRequest request, User customer) {
        if (customer.getRole().getName() != Role.RoleName.CUSTOMER) {
            throw new AccessDeniedException("Only customers can create procurement requests");
        }

        ProcurementRequest procurementRequest = new ProcurementRequest();
        procurementRequest.setTitle(request.getTitle());
        procurementRequest.setDescription(request.getDescription());
        procurementRequest.setBudget(request.getBudget());
        procurementRequest.setDueDate(request.getDueDate());
        procurementRequest.setStatus(RequestStatus.OPEN);
        procurementRequest.setCustomer(customer);

        ProcurementRequest savedRequest = procurementRequestRepository.save(procurementRequest);
        log.info("Created ProcurementRequest ID: {} for Customer ID: {}", savedRequest.getId(), customer.getId());
        return mapToDTO(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcurementRequestDTO> getMyRequests(User customer, RequestStatus status, String search,
            Pageable pageable) {
        Specification<ProcurementRequest> spec = Specification
                .where(ProcurementRequestSpecification.hasCustomerId(customer.getId()))
                .and(ProcurementRequestSpecification.hasStatus(status))
                .and(ProcurementRequestSpecification.search(search));
        Page<ProcurementRequest> requests = procurementRequestRepository.findAll(spec, pageable);
        return requests.map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProcurementRequestDTO> getAvailableRequests(RequestStatus status, String search, Pageable pageable) {
        RequestStatus filterStatus = status != null ? status : RequestStatus.OPEN;
        Specification<ProcurementRequest> spec = Specification
                .where(ProcurementRequestSpecification.hasStatus(filterStatus))
                .and(ProcurementRequestSpecification.search(search));
        Page<ProcurementRequest> requests = procurementRequestRepository.findAll(spec, pageable);
        return requests.map(this::mapToDTO);
    }

    private ProcurementRequestDTO mapToDTO(ProcurementRequest request) {
        ProcurementRequestDTO dto = new ProcurementRequestDTO();
        dto.setId(request.getId());
        dto.setTitle(request.getTitle());
        dto.setDescription(request.getDescription());
        dto.setBudget(request.getBudget());
        dto.setDueDate(request.getDueDate());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());
        return dto;
    }
}
