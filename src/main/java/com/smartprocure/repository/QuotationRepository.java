package com.smartprocure.repository;

import com.smartprocure.entity.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    List<Quotation> findByRequestId(Long requestId);
}
