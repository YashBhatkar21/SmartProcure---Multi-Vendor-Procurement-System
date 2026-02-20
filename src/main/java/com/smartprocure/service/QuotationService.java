package com.smartprocure.service;

import com.smartprocure.dto.CreateQuotationRequest;
import com.smartprocure.dto.QuotationDTO;
import com.smartprocure.entity.User;

import java.util.List;

public interface QuotationService {
    QuotationDTO submitQuotation(CreateQuotationRequest request, User vendorUser);

    List<QuotationDTO> getQuotationsForRequest(Long requestId, User user);

    List<QuotationDTO> getMyQuotations(User vendorUser);

    QuotationDTO acceptQuotation(Long quotationId, User customer);

    QuotationDTO rejectQuotation(Long quotationId, User customer);
}
