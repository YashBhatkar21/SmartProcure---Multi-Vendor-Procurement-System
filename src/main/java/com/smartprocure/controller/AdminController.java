package com.smartprocure.controller;

import com.smartprocure.dto.UserDTO;
import com.smartprocure.dto.VendorDTO;
import com.smartprocure.entity.Role;
import com.smartprocure.entity.User;
import com.smartprocure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.smartprocure.specification.UserSpecification;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class AdminController {

        private final UserRepository userRepository;

        @GetMapping("/users")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Page<UserDTO>> getAllUsers(
                        @RequestParam(required = false) String search,
                        Pageable pageable) {
                Specification<User> spec = Specification.where(UserSpecification.search(search));
                return ResponseEntity.ok(userRepository.findAll(spec, pageable)
                                .map(this::mapToDTO));
        }

        @GetMapping("/vendors")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Page<UserDTO>> getAllVendors(
                        @RequestParam(required = false) String search,
                        Pageable pageable) {
                Specification<User> spec = Specification.where(UserSpecification.hasRole(Role.RoleName.VENDOR))
                                .and(UserSpecification.search(search));
                return ResponseEntity.ok(userRepository.findAll(spec, pageable)
                                .map(this::mapToDTO));
        }

        private UserDTO mapToDTO(User user) {
                VendorDTO vendorDTO = null;
                if (user.getRole() != null && "VENDOR".equals(user.getRole().getName().name())
                                && user.getVendor() != null) {
                        vendorDTO = new VendorDTO(
                                        user.getVendor().getId(),
                                        user.getVendor().getCompanyName(),
                                        user.getVendor().getContactPhone(),
                                        user.getVendor().getAddress());
                }
                String roleName = user.getRole() != null ? user.getRole().getName().name() : "UNKNOWN";
                return new UserDTO(
                                user.getId(),
                                user.getEmail(),
                                user.getFullName(),
                                roleName,
                                vendorDTO);
        }
}
