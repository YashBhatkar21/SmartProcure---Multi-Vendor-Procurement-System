package com.smartprocure.specification;

import com.smartprocure.entity.Order;
import com.smartprocure.entity.OrderStatus;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecification {

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Order> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty())
                return null;
            return cb.like(cb.lower(root.get("orderNumber")), "%" + keyword.toLowerCase() + "%");
        };
    }

    public static Specification<Order> forCustomer(Long customerId) {
        return (root, query, cb) -> customerId == null ? null
                : cb.equal(root.get("quotation").get("request").get("customer").get("id"), customerId);
    }

    public static Specification<Order> forVendor(Long vendorId) {
        return (root, query, cb) -> vendorId == null ? null
                : cb.equal(root.get("quotation").get("vendor").get("id"), vendorId);
    }
}
