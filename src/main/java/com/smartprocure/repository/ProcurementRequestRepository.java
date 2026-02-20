package com.smartprocure.repository;

import com.smartprocure.entity.ProcurementRequest;
import com.smartprocure.entity.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcurementRequestRepository extends JpaRepository<ProcurementRequest, Long> {

    List<ProcurementRequest> findByCustomerId(Long customerId);

    Page<ProcurementRequest> findByCustomerId(Long customerId, Pageable pageable);

    List<ProcurementRequest> findByStatus(RequestStatus status);
}
