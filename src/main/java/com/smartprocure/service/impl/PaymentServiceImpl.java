package com.smartprocure.service.impl;

import com.smartprocure.dto.PaymentConfirmRequest;
import com.smartprocure.dto.PaymentDTO;
import com.smartprocure.dto.PaymentInitiateResponse;
import com.smartprocure.entity.Order;
import com.smartprocure.entity.OrderStatus;
import com.smartprocure.entity.Payment;
import com.smartprocure.entity.PaymentStatus;
import com.smartprocure.entity.User;
import com.smartprocure.entity.Vendor;
import com.smartprocure.repository.OrderRepository;
import com.smartprocure.repository.PaymentRepository;
import com.smartprocure.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public PaymentInitiateResponse initiatePayment(Long orderId, String method, User customer) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // Validate Ownership
        if (!order.getQuotation().getRequest().getCustomer().getId().equals(customer.getId())) {
            logger.warn("User {} attempted to initiate payment for unauthorized Order {}", customer.getEmail(),
                    orderId);
            throw new RuntimeException("Unauthorized: You do not own this order.");
        }

        // Prevent Duplicate Payments for Completed Orders
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Cannot initiate payment: Order is already completed.");
        }

        // Check if there's already an existing pending payment and return it if so
        // (Idempotency check / user clicked back and forth)
        List<Payment> existingPayments = paymentRepository.findByOrderId(orderId);
        for (Payment existing : existingPayments) {
            if (existing.getStatus() == PaymentStatus.PENDING) {
                logger.info("Resuming existing pending payment for Order {}", orderId);
                return buildInitiateResponse(existing, order);
            }
        }

        logger.info("Initiating new {} payment for Order {}", method, orderId);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(method);
        payment.setStatus(PaymentStatus.PENDING);
        // Generate Mock Secure Transaction ID
        payment.setTransactionId("TXN-" + UUID.randomUUID().toString().toUpperCase().substring(0, 18));

        Payment savedPayment = paymentRepository.save(payment);

        return buildInitiateResponse(savedPayment, order);
    }

    @Override
    @Transactional
    public PaymentDTO confirmPayment(PaymentConfirmRequest request) {
        Payment payment = paymentRepository.findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Payment not found for TXN: " + request.getTransactionId()));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            logger.warn("Attempt to confirm payment {} which is already in {} state", payment.getTransactionId(),
                    payment.getStatus());
            throw new RuntimeException("Payment is already processed.");
        }

        if (request.isSuccess()) {
            logger.info("Payment SUCCESS for TXN: {}", payment.getTransactionId());
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(Instant.now());
            if (request.getPaymentMethod() != null) {
                payment.setPaymentMethod(request.getPaymentMethod());
            }

            Order order = payment.getOrder();
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
        } else {
            logger.info("Payment FAILED for TXN: {}", payment.getTransactionId());
            payment.setStatus(PaymentStatus.FAILED);
        }

        Payment updatedPayment = paymentRepository.save(payment);
        return mapToDTO(updatedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getVendorPayments(Vendor vendor) {
        return paymentRepository.findByOrder_Quotation_Vendor(vendor).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getCustomerPayments(User customer) {
        return paymentRepository.findByOrder_Quotation_Request_Customer(customer).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private PaymentInitiateResponse buildInitiateResponse(Payment payment, Order order) {
        return PaymentInitiateResponse.builder()
                .transactionId(payment.getTransactionId())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .build();
    }

    private PaymentDTO mapToDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .orderId(payment.getOrder().getId())
                .orderNumber(payment.getOrder().getOrderNumber())
                .build();
    }
}
