package com.smartprocure.specification;

import com.smartprocure.entity.Role;
import com.smartprocure.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasRole(Role.RoleName roleName) {
        return (root, query, cb) -> {
            if (roleName == null)
                return null;
            return cb.equal(root.get("role").get("name"), roleName);
        };
    }

    public static Specification<User> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty())
                return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("fullName")), pattern));
        };
    }
}
