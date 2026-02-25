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

    @Query("SELECT new com.smartprocure.dto.dashboard.ChartSeriesDTO(FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m'), SUM(o.totalAmount)) "
            +
            "FROM Order o WHERE o.status = com.smartprocure.entity.OrderStatus.COMPLETED " +
            "GROUP BY FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m') ASC")
    java.util.List<com.smartprocure.dto.dashboard.ChartSeriesDTO<java.math.BigDecimal>> findMonthlyRevenue();

    @Query("SELECT new com.smartprocure.dto.dashboard.ChartSeriesDTO(FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m'), SUM(o.totalAmount)) "
            +
            "FROM Order o WHERE o.status = com.smartprocure.entity.OrderStatus.COMPLETED AND o.quotation.vendor = :vendor "
            +
            "GROUP BY FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m') ASC")
    java.util.List<com.smartprocure.dto.dashboard.ChartSeriesDTO<java.math.BigDecimal>> findMonthlyRevenueByVendor(
            @org.springframework.data.repository.query.Param("vendor") com.smartprocure.entity.Vendor vendor);

    @Query("SELECT new com.smartprocure.dto.dashboard.ChartSeriesDTO(o.quotation.request.customer.fullName, COUNT(o)) "
            +
            "FROM Order o " +
            "GROUP BY o.quotation.request.customer.fullName " +
            "ORDER BY COUNT(o) DESC")
    java.util.List<com.smartprocure.dto.dashboard.ChartSeriesDTO<Long>> findPurchasesPerCustomer();

    @Query("SELECT new com.smartprocure.dto.dashboard.ChartSeriesDTO(CAST(o.status AS string), COUNT(o)) " +
            "FROM Order o " +
            "GROUP BY o.status")
    java.util.List<com.smartprocure.dto.dashboard.ChartSeriesDTO<Long>> findOrderStatusDistribution();

    @Query("SELECT new com.smartprocure.dto.dashboard.ChartSeriesDTO(o.quotation.vendor.companyName, COUNT(o)) " +
            "FROM Order o WHERE o.status = com.smartprocure.entity.OrderStatus.COMPLETED " +
            "GROUP BY o.quotation.vendor.companyName " +
            "ORDER BY COUNT(o) DESC")
    java.util.List<com.smartprocure.dto.dashboard.ChartSeriesDTO<Long>> findVendorActivityOrders();

    long countByQuotation_VendorAndStatus(com.smartprocure.entity.Vendor vendor,
            com.smartprocure.entity.OrderStatus status);
}
