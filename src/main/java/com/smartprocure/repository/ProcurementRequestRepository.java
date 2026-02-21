package com.smartprocure.repository;

import com.smartprocure.entity.ProcurementRequest;
import com.smartprocure.entity.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ProcurementRequestRepository
        extends JpaRepository<ProcurementRequest, Long>, JpaSpecificationExecutor<ProcurementRequest> {

    List<ProcurementRequest> findByCustomerId(Long customerId);

    Page<ProcurementRequest> findByCustomerId(Long customerId, Pageable pageable);

    List<ProcurementRequest> findByStatus(RequestStatus status);

    Page<ProcurementRequest> findByStatus(RequestStatus status, Pageable pageable);
}
