package com.smartprocure.service;

import com.smartprocure.dto.auth.LoginRequest;
import com.smartprocure.dto.auth.LoginResponse;
import com.smartprocure.dto.auth.RegisterRequest;
import com.smartprocure.entity.Role;
import com.smartprocure.entity.Token;
import com.smartprocure.entity.User;
import com.smartprocure.repository.RoleRepository;
import com.smartprocure.repository.TokenRepository;
import com.smartprocure.repository.UserRepository;
import com.smartprocure.repository.VendorRepository;
import com.smartprocure.entity.Vendor;
import com.smartprocure.security.JwtService;
import com.smartprocure.security.SmartProcurePrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final RoleRepository roleRepository;
    private final VendorRepository vendorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${security.jwt.access-token-expiration-minutes}")
    private long accessTokenExpirationMinutes;

    public LoginResponse register(RegisterRequest request) {
        Role.RoleName roleName = request.getRole() != null ? request.getRole() : Role.RoleName.CUSTOMER;
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        User savedUser = userRepository.save(user);

        if (roleName == Role.RoleName.VENDOR) {
            Vendor vendor = new Vendor();
            vendor.setCompanyName(request.getFullName() + "'s Company");
            vendor.setContactPhone("Pending");
            vendor.setAddress("Pending");
            vendor.setUser(savedUser);
            vendorRepository.save(vendor);
            log.info("Provisioning Vendor profile for user: {}", savedUser.getEmail());
        }

        // Create Principal for token generation
        SmartProcurePrincipal principal = SmartProcurePrincipal.create(savedUser);
        String jwtToken = jwtService.generateAccessToken(principal);

        saveUserToken(savedUser, jwtToken);

        long expiresInSeconds = accessTokenExpirationMinutes * 60;
        return new LoginResponse(jwtToken, expiresInSeconds, role.getName().name(), savedUser.getFullName());
    }

    public LoginResponse authenticate(LoginRequest request) {
        log.info("Authentication attempt for user: {}", request.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SmartProcurePrincipal principal = (SmartProcurePrincipal) authentication.getPrincipal(); // Assuming this
                                                                                                 // returns your
                                                                                                 // principal
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        String jwtToken = jwtService.generateAccessToken(principal);

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        log.info("User authenticated successfully: {}", request.getEmail());

        long expiresInSeconds = accessTokenExpirationMinutes * 60;
        return new LoginResponse(jwtToken, expiresInSeconds, user.getRole().getName().name(), user.getFullName());
    }

    private void saveUserToken(User user, String jwtToken) {
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(Token.TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
}
