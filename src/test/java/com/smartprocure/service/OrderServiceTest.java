package com.smartprocure.service;

import com.smartprocure.dto.OrderDTO;
import com.smartprocure.entity.*;
import com.smartprocure.repository.OrderRepository;
import com.smartprocure.repository.QuotationRepository;
import com.smartprocure.repository.VendorRepository;
import com.smartprocure.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private VendorRepository vendorRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User customerUser;
    private User vendorUser;
    private Vendor vendor;
    private Quotation quotation;
    private ProcurementRequest request;

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
        vendorUser.setFullName("Vendor Name");

        vendor = new Vendor();
        vendor.setId(10L);
        vendor.setUser(vendorUser);
        vendor.setCompanyName("Vendor Company");

        request = new ProcurementRequest();
        request.setId(20L);
        request.setCustomer(customerUser);
        request.setTitle("Laptops Request");

        quotation = new Quotation();
        quotation.setId(30L);
        quotation.setVendor(vendor);
        quotation.setRequest(request);
        quotation.setQuotedAmount(new BigDecimal("5000.00"));
        quotation.setStatus(QuotationStatus.APPROVED);
    }

    @Test
    void testCreateOrder_Success() {
        Order mockOrder = new Order();
        mockOrder.setId(100L);
        mockOrder.setOrderNumber("ORD-1234");
        mockOrder.setQuotation(quotation);
        mockOrder.setTotalAmount(new BigDecimal("5000.00"));
        mockOrder.setStatus(OrderStatus.CREATED);

        when(quotationRepository.findById(30L)).thenReturn(Optional.of(quotation));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        OrderDTO result = orderService.createOrder(30L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("ORD-1234", result.getOrderNumber());
        assertEquals(OrderStatus.CREATED, result.getStatus());
        assertEquals(new BigDecimal("5000.00"), result.getTotalAmount());

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCreateOrder_WithNonApprovedQuotation_ThrowsIllegalStateException() {
        quotation.setStatus(QuotationStatus.SUBMITTED);
        when(quotationRepository.findById(30L)).thenReturn(Optional.of(quotation));

        assertThrows(IllegalStateException.class, () -> {
            orderService.createOrder(30L);
        });

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testUpdateOrderStatus_AsVendor_Success() {
        Order order = new Order();
        order.setId(100L);
        order.setQuotation(quotation);
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(vendorRepository.findByUser_Id(2L)).thenReturn(Optional.of(vendor));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDTO result = orderService.updateOrderStatus(100L, OrderStatus.PROCESSING, vendorUser);

        assertEquals(OrderStatus.PROCESSING, result.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testUpdateOrderStatus_AsWrongVendor_ThrowsAccessDeniedException() {
        Order order = new Order();
        order.setId(100L);

        Vendor wrongVendor = new Vendor();
        wrongVendor.setId(99L); // different vendor ID

        Quotation wrongQuotation = new Quotation();
        wrongQuotation.setVendor(wrongVendor);
        order.setQuotation(wrongQuotation);

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(vendorRepository.findByUser_Id(2L)).thenReturn(Optional.of(vendor));

        assertThrows(AccessDeniedException.class, () -> {
            orderService.updateOrderStatus(100L, OrderStatus.PROCESSING, vendorUser);
        });

        verify(orderRepository, never()).save(any(Order.class));
    }
}
