package com.smartprocure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartprocure.dto.auth.LoginRequest;
import com.smartprocure.dto.auth.RegisterRequest;
import com.smartprocure.entity.Role;
import com.smartprocure.repository.UserRepository;
import com.smartprocure.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(scripts = "classpath:fix_schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class FunctionalVerificationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private TokenRepository tokenRepository;

        @BeforeEach
        void setUp() {
                // Cleanup handled by @Transactional and @Sql
        }

        @Test
        void verifyNewUserRegistration() throws Exception {
                RegisterRequest request = new RegisterRequest(
                                "Brand New User",
                                "new.user.test@example.com",
                                "password123",
                                Role.RoleName.CUSTOMER);

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").exists());
        }

        @Test
        void verifyVendorLoginAndProfile() throws Exception {
                // 1. Login as Vendor (seeded by SeedDataRunner)
                LoginRequest loginRequest = new LoginRequest("vendor@smartprocure.com", "password");

                String token = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").exists())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                // Extract token
                String accessToken = objectMapper.readTree(token).get("accessToken").asText();

                // 2. Access Vendor Profile
                mockMvc.perform(get("/api/vendor/profile")
                                .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.companyName").exists());
        }

        @Test
        void verifyAdminLoginAndUserList() throws Exception {
                // 1. Login as Admin
                LoginRequest loginRequest = new LoginRequest("admin@smartprocure.com", "password");

                String token = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").exists())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                // Extract token
                String accessToken = objectMapper.readTree(token).get("accessToken").asText();

                // 2. List Users
                mockMvc.perform(get("/api/admin/users")
                                .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].email").exists());

                // 3. List Vendors
                mockMvc.perform(get("/api/admin/vendors")
                                .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].email").exists())
                                .andExpect(jsonPath("$[0].vendor").exists())
                                .andExpect(jsonPath("$[0].vendor.companyName").exists());
        }
}
