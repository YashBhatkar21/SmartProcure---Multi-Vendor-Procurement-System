package com.smartprocure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartprocure.dto.auth.LoginRequest;
import com.smartprocure.dto.auth.RegisterRequest;
import com.smartprocure.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(scripts = "classpath:fix_schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class SecurityIntegrationTest {

        @Autowired
        private com.smartprocure.repository.UserRepository userRepository;

        @Autowired
        private com.smartprocure.repository.TokenRepository tokenRepository;

        @org.junit.jupiter.api.BeforeEach
        void setUp() {
                userRepository.findByEmail("testuser@example.com").ifPresent(user -> {
                        tokenRepository.deleteByUser(user);
                        userRepository.delete(user);
                });
        }

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void testRegistrationAndLogin() throws Exception {
                // 1. Register
                RegisterRequest registerRequest = new RegisterRequest("Test User", "testuser@example.com", "password",
                                Role.RoleName.CUSTOMER);

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").exists());

                // 2. Login
                LoginRequest loginRequest = new LoginRequest("testuser@example.com", "password");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").exists());
        }

        @Test
        void testRoleAccess() throws Exception {
                // Login as Admin (Pre-seeded)
                LoginRequest adminLogin = new LoginRequest("admin@smartprocure.com", "password");
                String adminToken = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(adminLogin)))
                                .andReturn().getResponse().getContentAsString();

                // Extract token manually or via helper
                adminToken = new ObjectMapper().readTree(adminToken).get("accessToken").asText();

                // Admin accessing Admin Endpoint -> OK
                mockMvc.perform(get("/api/admin/users")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk());

                // Admin accessing Vendor Endpoint -> Forbidden
                mockMvc.perform(get("/api/vendor/profile")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isForbidden());
        }

        @Test
        void testDuplicateRegistration() throws Exception {
                // 1. Register first time (Success)
                RegisterRequest registerRequest = new RegisterRequest("Dup User", "dup@example.com", "password",
                                Role.RoleName.CUSTOMER);

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isOk());

                // 2. Register second time (Should fail with 409 Conflict, but currently 500)
                // We expect 500 for now to confirm reproduction, or we expect 409 if we want to
                // write the test for the fix.
                // Let's expect 409 to show it fails (as it will return 500).
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                                .andExpect(status().isConflict());
        }
}
