package com.smartprocure.service;

import com.smartprocure.dto.CreateQuotationRequest;
import com.smartprocure.dto.QuotationDTO;
import com.smartprocure.entity.*;
import com.smartprocure.repository.ProcurementRequestRepository;
import com.smartprocure.repository.QuotationRepository;
import com.smartprocure.repository.VendorRepository;
import com.smartprocure.service.impl.QuotationServiceImpl;
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
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuotationServiceTest {

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private ProcurementRequestRepository procurementRequestRepository;

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private QuotationServiceImpl quotationService;

    private User customerUser;
    private User vendorUser;
    private Vendor vendor;
    private ProcurementRequest request;
    private Quotation quotation;

    @BeforeEach
    void setUp() {
        Role customerRole = new Role();
        customerRole.setName(Role.RoleName.CUSTOMER);

        customerUser = new User();
        customerUser.setId(1L);
        customerUser.setRole(customerRole);
        customerUser.setFullName("Customer Name");

        Role vendorRole = new Role();
        vendorRole.setName(Role.RoleName.VENDOR);

        vendorUser = new User();
        vendorUser.setId(2L);
        vendorUser.setRole(vendorRole);

        vendor = new Vendor();
        vendor.setId(10L);
        vendor.setUser(vendorUser);
        vendor.setCompanyName("Vendor Company");

        request = new ProcurementRequest();
        request.setId(20L);
        request.setCustomer(customerUser);
        request.setTitle("Laptops Request");
        request.setStatus(RequestStatus.OPEN);

        quotation = new Quotation();
        quotation.setId(30L);
        quotation.setVendor(vendor);
        quotation.setRequest(request);
        quotation.setQuotedAmount(new BigDecimal("4500.00"));
        quotation.setStatus(QuotationStatus.SUBMITTED);
    }

    @Test
    void testSubmitQuotation_Success() {
        CreateQuotationRequest createReq = new CreateQuotationRequest();
        createReq.setRequestId(20L);
        createReq.setQuotedAmount(new BigDecimal("4500.00"));
        createReq.setTerms("Standard term");
        createReq.setValidUntil(Instant.now().plus(5, ChronoUnit.DAYS));

        when(vendorRepository.findByUser_Id(2L)).thenReturn(Optional.of(vendor));
        when(procurementRequestRepository.findById(20L)).thenReturn(Optional.of(request));
        when(quotationRepository.findByRequestAndVendor(request, vendor)).thenReturn(Optional.empty());
        when(quotationRepository.save(any(Quotation.class))).thenReturn(quotation);

        QuotationDTO result = quotationService.submitQuotation(createReq, vendorUser);

        assertNotNull(result);
        assertEquals(30L, result.getId());
        assertEquals(new BigDecimal("4500.00"), result.getQuotedAmount());
        assertEquals(QuotationStatus.SUBMITTED, result.getStatus());
        verify(quotationRepository, times(1)).save(any(Quotation.class));
    }

    @Test
    void testSubmitQuotation_Duplicate_ThrowsIllegalStateException() {
        CreateQuotationRequest createReq = new CreateQuotationRequest();
        createReq.setRequestId(20L);

        when(vendorRepository.findByUser_Id(2L)).thenReturn(Optional.of(vendor));
        when(procurementRequestRepository.findById(20L)).thenReturn(Optional.of(request));
        when(quotationRepository.findByRequestAndVendor(request, vendor)).thenReturn(Optional.of(quotation)); // Already
                                                                                                              // exists

        assertThrows(IllegalStateException.class, () -> {
            quotationService.submitQuotation(createReq, vendorUser);
        });

        verify(quotationRepository, never()).save(any(Quotation.class));
    }

    @Test
    void testAcceptQuotation_Success() {
        when(quotationRepository.findById(30L)).thenReturn(Optional.of(quotation));
        when(quotationRepository.findByRequest(request)).thenReturn(Collections.singletonList(quotation));
        when(quotationRepository.save(any(Quotation.class))).thenReturn(quotation);
        
        // Mock order creation
        when(orderService.createOrder(30L)).thenReturn(null);

        QuotationDTO result = quotationService.acceptQuotation(30L, customerUser);

        assertEquals(QuotationStatus.APPROVED, result.getStatus());
        assertEquals(RequestStatus.APPROVED, request.getStatus());
        verify(procurementRequestRepository, times(1)).save(request);
        verify(orderService, times(1)).createOrder(30L);
    }

    @Test
    void testAcceptQuotation_WrongCustomer_ThrowsAccessDeniedException() {
        User wrongCustomer = new User();
        wrongCustomer.setId(99L); // Different ID
        Role role = new Role();
        role.setName(Role.RoleName.CUSTOMER);
        wrongCustomer.setRole(role);

        when(quotationRepository.findById(30L)).thenReturn(Optional.of(quotation));

        assertThrows(AccessDeniedException.class, () -> {
            quotationService.acceptQuotation(30L, wrongCustomer);
        });

        verify(orderService, never()).createOrder(anyLong());
    }
}
