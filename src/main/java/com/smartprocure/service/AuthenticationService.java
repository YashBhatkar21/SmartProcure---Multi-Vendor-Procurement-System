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
import com.smartprocure.security.JwtService;
import com.smartprocure.security.SmartProcurePrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final RoleRepository roleRepository;
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

        // Create Principal for token generation
        SmartProcurePrincipal principal = SmartProcurePrincipal.create(savedUser);
        String jwtToken = jwtService.generateAccessToken(principal);

        saveUserToken(savedUser, jwtToken);

        long expiresInSeconds = accessTokenExpirationMinutes * 60;
        return new LoginResponse(jwtToken, expiresInSeconds, role.getName().name());
    }

    public LoginResponse authenticate(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SmartProcurePrincipal principal = (SmartProcurePrincipal) authentication.getPrincipal(); // Assuming this
                                                                                                 // returns your
                                                                                                 // principal
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        String jwtToken = jwtService.generateAccessToken(principal);

        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        long expiresInSeconds = accessTokenExpirationMinutes * 60;
        return new LoginResponse(jwtToken, expiresInSeconds, user.getRole().getName().name());
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
