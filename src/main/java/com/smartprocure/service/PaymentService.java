package com.smartprocure.service;

import com.smartprocure.dto.PaymentConfirmRequest;
import com.smartprocure.dto.PaymentDTO;
import com.smartprocure.dto.PaymentInitiateResponse;
import com.smartprocure.entity.User;
import com.smartprocure.entity.Vendor;

import java.util.List;

public interface PaymentService {

    PaymentInitiateResponse initiatePayment(Long orderId, String method, User customer);

    PaymentDTO confirmPayment(PaymentConfirmRequest request);

    List<PaymentDTO> getVendorPayments(Vendor vendor);

    List<PaymentDTO> getCustomerPayments(User customer);
}
