package com.smartprocure.config;

import com.smartprocure.entity.Role;
import com.smartprocure.entity.User;
import com.smartprocure.entity.Vendor;
import com.smartprocure.repository.RoleRepository;
import com.smartprocure.repository.UserRepository;
import com.smartprocure.repository.VendorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Inserts sample data only when the database is empty (e.g. first run).
 * Uses BCrypt for passwords; no dependency on spring-boot-starter-security
 * for encoding is required if we avoid it here - but we will need it for Day 3.
 * For now we store a pre-encoded BCrypt hash so no PasswordEncoder is needed at seed time.
 */
@Configuration
public class SeedDataRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataRunner.class);

    @Bean
    @Order(1)
    CommandLineRunner seedData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            VendorRepository vendorRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            log.info("Ensuring seed data exists: roles, users, vendor.");
            String seedPasswordHash = passwordEncoder.encode("password");

            Role adminRole = roleRepository.findByName(Role.RoleName.ADMIN)
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setName(Role.RoleName.ADMIN);
                        return roleRepository.save(r);
                    });

            Role customerRole = roleRepository.findByName(Role.RoleName.CUSTOMER)
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setName(Role.RoleName.CUSTOMER);
                        return roleRepository.save(r);
                    });

            Role vendorRole = roleRepository.findByName(Role.RoleName.VENDOR)
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setName(Role.RoleName.VENDOR);
                        return roleRepository.save(r);
                    });

            User admin = userRepository.findByEmail("admin@smartprocure.com").orElseGet(User::new);
            admin.setEmail("admin@smartprocure.com");
            admin.setPasswordHash(seedPasswordHash);
            if (admin.getFullName() == null || admin.getFullName().isBlank()) {
                admin.setFullName("Admin User");
            }
            admin.setRole(adminRole);
            userRepository.save(admin);

            User customer = userRepository.findByEmail("customer@smartprocure.com").orElseGet(User::new);
            customer.setEmail("customer@smartprocure.com");
            customer.setPasswordHash(seedPasswordHash);
            if (customer.getFullName() == null || customer.getFullName().isBlank()) {
                customer.setFullName("Customer User");
            }
            customer.setRole(customerRole);
            userRepository.save(customer);

            User vendorUser = userRepository.findByEmail("vendor@smartprocure.com").orElseGet(User::new);
            vendorUser.setEmail("vendor@smartprocure.com");
            vendorUser.setPasswordHash(seedPasswordHash);
            if (vendorUser.getFullName() == null || vendorUser.getFullName().isBlank()) {
                vendorUser.setFullName("Vendor User");
            }
            vendorUser.setRole(vendorRole);
            User savedVendorUser = userRepository.save(vendorUser);

            vendorRepository.findByUser_Id(savedVendorUser.getId()).orElseGet(() -> {
                Vendor vendor = new Vendor();
                vendor.setCompanyName("Acme Supplies Ltd");
                vendor.setContactPhone("+1-555-0100");
                vendor.setAddress("123 Commerce St");
                vendor.setUser(savedVendorUser);
                return vendorRepository.save(vendor);
            });
        };
    }
}
