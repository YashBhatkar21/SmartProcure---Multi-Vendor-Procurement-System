package com.smartprocure.repository;

import com.smartprocure.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.smartprocure.entity.Role;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @EntityGraph(attributePaths = "role")
    Optional<User> findByEmail(String email);

    Page<User> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "role")
    Page<User> findByRole_Name(Role.RoleName roleName, Pageable pageable);

    long countByRole_Name(Role.RoleName roleName);
}
