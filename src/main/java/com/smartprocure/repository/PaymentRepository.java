package com.smartprocure.repository;

import com.smartprocure.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

        List<Payment> findByOrderId(Long orderId);

        java.util.Optional<Payment> findByTransactionId(String transactionId);

        List<Payment> findByOrder_Quotation_Vendor(com.smartprocure.entity.Vendor vendor);

        List<Payment> findByOrder_Quotation_Request_Customer(com.smartprocure.entity.User customer);

        @org.springframework.data.jpa.repository.Query("SELECT SUM(p.amount) FROM Payment p WHERE p.order.quotation.vendor = :vendor AND p.status = com.smartprocure.entity.PaymentStatus.PENDING")
        java.math.BigDecimal sumPendingPaymentsByVendor(
                        @org.springframework.data.repository.query.Param("vendor") com.smartprocure.entity.Vendor vendor);

        @org.springframework.data.jpa.repository.Query("SELECT p FROM Payment p WHERE p.order.quotation.vendor = :vendor ORDER BY p.createdAt DESC")
        List<Payment> findRecentPaymentsByVendor(
                        @org.springframework.data.repository.query.Param("vendor") com.smartprocure.entity.Vendor vendor,
                        org.springframework.data.domain.Pageable pageable);
}
