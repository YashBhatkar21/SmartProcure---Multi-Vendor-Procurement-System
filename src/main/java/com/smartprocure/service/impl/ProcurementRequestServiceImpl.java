package com.smartprocure.service.impl;

import com.smartprocure.dto.CreateProcurementRequestRequest;
import com.smartprocure.dto.ProcurementRequestDTO;
import com.smartprocure.entity.ProcurementRequest;
import com.smartprocure.entity.RequestStatus;
import com.smartprocure.entity.User;
import com.smartprocure.repository.ProcurementRequestRepository;
import com.smartprocure.service.ProcurementRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcurementRequestServiceImpl implements ProcurementRequestService {

    private final ProcurementRequestRepository procurementRequestRepository;

    @Override
    @Transactional
    public ProcurementRequestDTO createRequest(CreateProcurementRequestRequest request, User customer) {
        ProcurementRequest procurementRequest = new ProcurementRequest();
        procurementRequest.setTitle(request.getTitle());
        procurementRequest.setDescription(request.getDescription());
        procurementRequest.setBudget(request.getBudget());
        procurementRequest.setDueDate(request.getDueDate());
        procurementRequest.setStatus(RequestStatus.OPEN);
        procurementRequest.setCustomer(customer);

        ProcurementRequest savedRequest = procurementRequestRepository.save(procurementRequest);
        return mapToDTO(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcurementRequestDTO> getMyRequests(User customer) {
        List<ProcurementRequest> requests = procurementRequestRepository.findByCustomerId(customer.getId());
        return requests.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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
