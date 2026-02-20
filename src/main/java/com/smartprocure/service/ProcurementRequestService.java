package com.smartprocure.service;

import com.smartprocure.dto.CreateProcurementRequestRequest;
import com.smartprocure.dto.ProcurementRequestDTO;
import com.smartprocure.entity.User;

import java.util.List;

public interface ProcurementRequestService {
    ProcurementRequestDTO createRequest(CreateProcurementRequestRequest request, User customer);

    List<ProcurementRequestDTO> getMyRequests(User customer);
}
