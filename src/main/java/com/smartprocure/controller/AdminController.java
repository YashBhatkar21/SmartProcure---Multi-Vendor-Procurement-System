package com.smartprocure.controller;

import com.smartprocure.dto.UserDTO;
import com.smartprocure.dto.VendorDTO;
import com.smartprocure.entity.User;
import com.smartprocure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList());
    }

    @GetMapping("/vendors")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllVendors() {
        return ResponseEntity.ok(userRepository.findAll().stream()
                .filter(user -> "VENDOR".equals(user.getRole().getName().name()))
                .map(this::mapToDTO)
                .toList());
    }

    private UserDTO mapToDTO(User user) {
        VendorDTO vendorDTO = null;
        if (user.getVendor() != null) {
            vendorDTO = new VendorDTO(
                    user.getVendor().getId(),
                    user.getVendor().getCompanyName(),
                    user.getVendor().getContactPhone(),
                    user.getVendor().getAddress());
        }
        return new UserDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().getName().name(),
                vendorDTO);
    }
}
