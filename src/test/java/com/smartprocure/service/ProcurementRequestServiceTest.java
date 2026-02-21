package com.smartprocure.service;

import com.smartprocure.dto.CreateProcurementRequestRequest;
import com.smartprocure.dto.ProcurementRequestDTO;
import com.smartprocure.entity.ProcurementRequest;
import com.smartprocure.entity.RequestStatus;
import com.smartprocure.entity.Role;
import com.smartprocure.entity.User;
import com.smartprocure.repository.ProcurementRequestRepository;
import com.smartprocure.repository.UserRepository;
import com.smartprocure.service.impl.ProcurementRequestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProcurementRequestServiceTest {

    @Mock
    private ProcurementRequestRepository procurementRequestRepository;

    @InjectMocks
    private ProcurementRequestServiceImpl procurementRequestService;

    private User customerUser;
    private User vendorUser;

    @BeforeEach
    void setUp() {
        Role customerRole = new Role();
        customerRole.setName(Role.RoleName.CUSTOMER);

        customerUser = new User();
        customerUser.setId(1L);
        customerUser.setRole(customerRole);
        customerUser.setFullName("Test Customer");

        Role vendorRole = new Role();
        vendorRole.setName(Role.RoleName.VENDOR);

        vendorUser = new User();
        vendorUser.setId(2L);
        vendorUser.setRole(vendorRole);
        vendorUser.setFullName("Test Vendor");
    }

    @Test
    void testCreateRequest_Success() {
        CreateProcurementRequestRequest createReq = new CreateProcurementRequestRequest();
        createReq.setTitle("New Laptops");
        createReq.setBudget(new BigDecimal("5000.00"));
        createReq.setDueDate(Instant.now().plus(5, ChronoUnit.DAYS));

        ProcurementRequest mockSavedReq = new ProcurementRequest();
        mockSavedReq.setId(10L);
        mockSavedReq.setTitle("New Laptops");
        mockSavedReq.setBudget(new BigDecimal("5000.00"));
        mockSavedReq.setStatus(RequestStatus.OPEN);
        mockSavedReq.setCustomer(customerUser);

        when(procurementRequestRepository.save(any(ProcurementRequest.class))).thenReturn(mockSavedReq);

        ProcurementRequestDTO result = procurementRequestService.createRequest(createReq, customerUser);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("New Laptops", result.getTitle());
        assertEquals(RequestStatus.OPEN, result.getStatus());
        verify(procurementRequestRepository, times(1)).save(any(ProcurementRequest.class));
    }

    @Test
    void testCreateRequest_AsVendor_ThrowsAccessDeniedException() {
        CreateProcurementRequestRequest createReq = new CreateProcurementRequestRequest();

        assertThrows(AccessDeniedException.class, () -> {
            procurementRequestService.createRequest(createReq, vendorUser);
        });

        verify(procurementRequestRepository, never()).save(any(ProcurementRequest.class));
    }
}
