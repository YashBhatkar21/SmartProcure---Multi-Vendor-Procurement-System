package com.smartprocure.specification;

import com.smartprocure.entity.ProcurementRequest;
import com.smartprocure.entity.RequestStatus;
import org.springframework.data.jpa.domain.Specification;

public class ProcurementRequestSpecification {

    public static Specification<ProcurementRequest> hasStatus(RequestStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<ProcurementRequest> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty())
                return null;
            return cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
        };
    }

    public static Specification<ProcurementRequest> hasCustomerId(Long customerId) {
        return (root, query, cb) -> customerId == null ? null : cb.equal(root.get("customer").get("id"), customerId);
    }
}
