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

        when(procurementRequestRepository.findByCustomerId(customer.getId())).thenReturn(List.of(request));

        List<ProcurementRequestDTO> results = procurementRequestService.getMyRequests(customer);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(request.getTitle(), results.get(0).getTitle());
    }
}
