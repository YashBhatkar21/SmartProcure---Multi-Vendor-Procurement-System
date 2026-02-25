package com.smartprocure.repository;

import com.smartprocure.entity.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuotationRepository extends JpaRepository<Quotation, Long> {

    List<Quotation> findByRequest(com.smartprocure.entity.ProcurementRequest request);

    List<Quotation> findByVendor(com.smartprocure.entity.Vendor vendor);

    java.util.Optional<Quotation> findByRequestAndVendor(com.smartprocure.entity.ProcurementRequest request,
            com.smartprocure.entity.Vendor vendor);

    @org.springframework.data.jpa.repository.Query("SELECT new com.smartprocure.dto.dashboard.ChartSeriesDTO(q.vendor.companyName, COUNT(q)) "
            +
            "FROM Quotation q " +
            "GROUP BY q.vendor.companyName " +
            "ORDER BY COUNT(q) DESC")
    java.util.List<com.smartprocure.dto.dashboard.ChartSeriesDTO<Long>> findVendorActivityQuotations();
}
