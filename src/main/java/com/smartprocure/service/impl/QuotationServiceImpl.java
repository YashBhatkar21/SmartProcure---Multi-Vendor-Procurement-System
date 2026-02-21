package com.smartprocure.service.impl;

import com.smartprocure.dto.CreateQuotationRequest;
import com.smartprocure.dto.QuotationDTO;
import com.smartprocure.entity.*;
import com.smartprocure.repository.ProcurementRequestRepository;
import com.smartprocure.repository.QuotationRepository;
import com.smartprocure.repository.VendorRepository;
import com.smartprocure.service.OrderService;
import com.smartprocure.service.QuotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuotationServiceImpl implements QuotationService {

    private final QuotationRepository quotationRepository;
    private final ProcurementRequestRepository procurementRequestRepository;
    private final VendorRepository vendorRepository;
    private final OrderService orderService;

    @Override
    @Transactional
    public QuotationDTO submitQuotation(CreateQuotationRequest request, User vendorUser) {
        if (vendorUser.getRole().getName() != Role.RoleName.VENDOR) {
            throw new AccessDeniedException("Only vendors can submit quotations");
        }

        Vendor vendor = vendorRepository.findByUser_Id(vendorUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Vendor profile not found for user"));

        ProcurementRequest procurementRequest = procurementRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Procurement Request not found"));

        if (procurementRequest.getStatus() != RequestStatus.OPEN) {
            throw new IllegalStateException("Cannot submit quotation for a closed or completed request");
        }

        // Check if vendor already submitted a quotation
        if (quotationRepository.findByRequestAndVendor(procurementRequest, vendor).isPresent()) {
            throw new IllegalStateException("Vendor has already submitted a quotation for this request");
        }

        Quotation quotation = new Quotation();
        quotation.setRequest(procurementRequest);
        quotation.setVendor(vendor);
        quotation.setQuotedAmount(request.getQuotedAmount());
        quotation.setTerms(request.getTerms());
        quotation.setValidUntil(request.getValidUntil());
        quotation.setStatus(QuotationStatus.SUBMITTED);

        Quotation savedQuotation = quotationRepository.save(quotation);
        return mapToDTO(savedQuotation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuotationDTO> getQuotationsForRequest(Long requestId, User user) {
        ProcurementRequest request = procurementRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        // Only the customer who created the request OR an Admin can view its quotations
        if (user.getRole().getName() == Role.RoleName.CUSTOMER && !request.getCustomer().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not authorized to view quotations for this request");
        }

        List<Quotation> quotations = quotationRepository.findByRequest(request);
        return quotations.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuotationDTO> getMyQuotations(User vendorUser) {
        if (vendorUser.getRole().getName() != Role.RoleName.VENDOR) {
            throw new AccessDeniedException("Only vendors can view their quotations");
        }

        Vendor vendor = vendorRepository.findByUser_Id(vendorUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Vendor profile not found"));

        List<Quotation> quotations = quotationRepository.findByVendor(vendor);
        return quotations.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuotationDTO acceptQuotation(Long quotationId, User customer) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found"));

        if (!quotation.getRequest().getCustomer().getId().equals(customer.getId())) {
            throw new AccessDeniedException("Not authorized to accept this quotation");
        }

        if (quotation.getRequest().getStatus() != RequestStatus.OPEN) {
            throw new IllegalStateException("Request is no longer open");
        }

        if (quotation.getStatus() != QuotationStatus.SUBMITTED) {
            throw new IllegalStateException("Quotation is not in a submittable state");
        }

        // Accept the quotation
        quotation.setStatus(QuotationStatus.APPROVED);

        // Update request status
        ProcurementRequest request = quotation.getRequest();
        request.setStatus(RequestStatus.APPROVED); // Or some other state indicating an accepted quote
        procurementRequestRepository.save(request);

        // Reject other quotations for the same request
        List<Quotation> otherQuotations = quotationRepository.findByRequest(request);
        for (Quotation other : otherQuotations) {
            if (!other.getId().equals(quotation.getId()) && other.getStatus() == QuotationStatus.SUBMITTED) {
                other.setStatus(QuotationStatus.REJECTED);
                quotationRepository.save(other);
            }
        }

        Quotation savedQuotation = quotationRepository.save(quotation);

        // Auto-generate the Purchase Order
        orderService.createOrder(savedQuotation.getId());

        return mapToDTO(savedQuotation);
    }

    @Override
    @Transactional
    public QuotationDTO rejectQuotation(Long quotationId, User customer) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found"));

        if (!quotation.getRequest().getCustomer().getId().equals(customer.getId())) {
            throw new AccessDeniedException("Not authorized to reject this quotation");
        }

        if (quotation.getStatus() != QuotationStatus.SUBMITTED) {
            throw new IllegalStateException("Quotation is already " + quotation.getStatus());
        }

        quotation.setStatus(QuotationStatus.REJECTED);
        return mapToDTO(quotationRepository.save(quotation));
    }

    private QuotationDTO mapToDTO(Quotation quotation) {
        return QuotationDTO.builder()
                .id(quotation.getId())
                .requestId(quotation.getRequest().getId())
                .vendorId(quotation.getVendor().getId())
                .vendorName(quotation.getVendor().getCompanyName())
                .quotedAmount(quotation.getQuotedAmount())
                .validUntil(quotation.getValidUntil())
                .terms(quotation.getTerms())
                .status(quotation.getStatus())
                .createdAt(quotation.getCreatedAt())
                .build();
    }
}
