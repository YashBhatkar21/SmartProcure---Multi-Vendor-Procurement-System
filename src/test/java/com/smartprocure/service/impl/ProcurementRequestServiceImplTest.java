package com.smartprocure.service.impl;

import com.smartprocure.dto.CreateProcurementRequestRequest;
import com.smartprocure.dto.ProcurementRequestDTO;
import com.smartprocure.entity.ProcurementRequest;
import com.smartprocure.entity.User;
import com.smartprocure.repository.ProcurementRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcurementRequestServiceImplTest {

    @Mock
    private ProcurementRequestRepository procurementRequestRepository;

    @InjectMocks
    private ProcurementRequestServiceImpl procurementRequestService;

    private User customer;
    private CreateProcurementRequestRequest createRequest;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId(1L);
        com.smartprocure.entity.Role role = new com.smartprocure.entity.Role();
        role.setName(com.smartprocure.entity.Role.RoleName.CUSTOMER);
        customer.setRole(role);
        customer.setEmail("customer@test.com");

        createRequest = new CreateProcurementRequestRequest();
        createRequest.setTitle("Test Request");
        createRequest.setDescription("Test Description");
        createRequest.setBudget(BigDecimal.valueOf(1000));
        createRequest.setDueDate(Instant.now().plusSeconds(3600));
    }

    @Test
    void createRequest_ShouldSaveAndReturnDTO() {
        ProcurementRequest savedEntity = new ProcurementRequest();
        savedEntity.setId(10L);
        savedEntity.setTitle(createRequest.getTitle());
        savedEntity.setDescription(createRequest.getDescription());
        savedEntity.setBudget(createRequest.getBudget());
        savedEntity.setDueDate(createRequest.getDueDate());
        savedEntity.setCustomer(customer);

        when(procurementRequestRepository.save(any(ProcurementRequest.class))).thenReturn(savedEntity);

        ProcurementRequestDTO result = procurementRequestService.createRequest(createRequest, customer);

        assertNotNull(result);
        assertEquals(savedEntity.getId(), result.getId());
        assertEquals(createRequest.getTitle(), result.getTitle());
        verify(procurementRequestRepository).save(any(ProcurementRequest.class));
    }

    @Test
    void getMyRequests_ShouldReturnList() {
        ProcurementRequest request = new ProcurementRequest();
        request.setId(10L);
        request.setTitle("Test Request");
        request.setCustomer(customer);

        Pageable pageable = PageRequest.of(0, 10);
        Page<ProcurementRequest> page = new PageImpl<>(List.of(request));

        // It's using JpaSpecificationExecutor now. So we must mock
        // findall(Specification, Pageable)
        when(procurementRequestRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                eq(pageable))).thenReturn(page);

        Page<ProcurementRequestDTO> results = procurementRequestService.getMyRequests(customer, null, null, pageable);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.getTotalElements());
        assertEquals(request.getTitle(), results.getContent().get(0).getTitle());
    }
}
