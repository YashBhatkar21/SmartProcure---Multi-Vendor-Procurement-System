package com.smartprocure.repository;

import com.smartprocure.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByOrderNumber(String orderNumber);

    java.util.List<Order> findByQuotation_Request_Customer(com.smartprocure.entity.User customer);

    Page<Order> findByQuotation_Request_Customer(com.smartprocure.entity.User customer, Pageable pageable);

    java.util.List<Order> findByQuotation_Vendor(com.smartprocure.entity.Vendor vendor);

    Page<Order> findByQuotation_Vendor(com.smartprocure.entity.Vendor vendor, Pageable pageable);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = com.smartprocure.entity.OrderStatus.COMPLETED")
    java.math.BigDecimal calculateTotalRevenue();
}
