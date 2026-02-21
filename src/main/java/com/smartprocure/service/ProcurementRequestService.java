package com.smartprocure.service;

import com.smartprocure.dto.CreateProcurementRequestRequest;
import com.smartprocure.dto.ProcurementRequestDTO;
import com.smartprocure.entity.RequestStatus;
import com.smartprocure.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProcurementRequestService {
    ProcurementRequestDTO createRequest(CreateProcurementRequestRequest request, User customer);

    Page<ProcurementRequestDTO> getMyRequests(User customer, RequestStatus status, String search, Pageable pageable);

    Page<ProcurementRequestDTO> getAvailableRequests(RequestStatus status, String search, Pageable pageable);
}
