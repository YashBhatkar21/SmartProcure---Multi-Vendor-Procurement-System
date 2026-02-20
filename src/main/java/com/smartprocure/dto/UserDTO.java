package com.smartprocure.dto;

public record UserDTO(
        Long id,
        String email,
        String fullName,
        String role,
        VendorDTO vendor) {
}
