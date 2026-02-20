package com.smartprocure.repository;

import com.smartprocure.entity.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    List<Quotation> findByRequest(com.smartprocure.entity.ProcurementRequest request);

    List<Quotation> findByVendor(com.smartprocure.entity.Vendor vendor);

    java.util.Optional<Quotation> findByRequestAndVendor(com.smartprocure.entity.ProcurementRequest request,
            com.smartprocure.entity.Vendor vendor);
}
