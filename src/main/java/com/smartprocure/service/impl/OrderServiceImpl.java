package com.smartprocure.service.impl;

import com.smartprocure.dto.OrderDTO;
import com.smartprocure.entity.*;
import com.smartprocure.repository.OrderRepository;
import com.smartprocure.repository.QuotationRepository;
import com.smartprocure.repository.VendorRepository;
import com.smartprocure.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.smartprocure.specification.OrderSpecification;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final QuotationRepository quotationRepository;
    private final VendorRepository vendorRepository;

    @Override
    @Transactional
    public OrderDTO createOrder(Long quotationId) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found"));

        if (quotation.getStatus() != QuotationStatus.APPROVED) {
            throw new IllegalStateException("Order can only be created from an APPROVED quotation");
        }

        Order order = new Order();
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        order.setOrderNumber(orderNumber);
        order.setQuotation(quotation);
        order.setTotalAmount(quotation.getQuotedAmount());
        order.setStatus(OrderStatus.CREATED);

        Order savedOrder = orderRepository.save(order);
        log.info("Created new Order '{}' from Quotation ID: {}", savedOrder.getOrderNumber(), quotation.getId());
        return mapToDTO(savedOrder);
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (user.getRole().getName() == Role.RoleName.VENDOR) {
            Vendor vendor = vendorRepository.findByUser_Id(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Vendor profile not found"));

            if (!order.getQuotation().getVendor().getId().equals(vendor.getId())) {
                throw new AccessDeniedException("You are not the vendor for this order");
            }
        } else if (user.getRole().getName() == Role.RoleName.CUSTOMER) {
            if (!order.getQuotation().getRequest().getCustomer().getId().equals(user.getId())) {
                throw new AccessDeniedException("You are not the customer for this order");
            }
        } else if (user.getRole().getName() != Role.RoleName.ADMIN) {
            throw new AccessDeniedException("Not authorized to update this order");
        }

        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);
        log.info("Updated status of Order ID: {} to {}", orderId, newStatus);
        return mapToDTO(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getCustomerOrders(User customer, OrderStatus status, String search, Pageable pageable) {
        Specification<Order> spec = Specification.where(OrderSpecification.forCustomer(customer.getId()))
                .and(OrderSpecification.hasStatus(status))
                .and(OrderSpecification.search(search));
        return orderRepository.findAll(spec, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderDTO> getVendorOrders(User vendorUser, OrderStatus status, String search, Pageable pageable) {
        Vendor vendor = vendorRepository.findByUser_Id(vendorUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Vendor profile not found"));
        Specification<Order> spec = Specification.where(OrderSpecification.forVendor(vendor.getId()))
                .and(OrderSpecification.hasStatus(status))
                .and(OrderSpecification.search(search));
        return orderRepository.findAll(spec, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (user.getRole().getName() == Role.RoleName.VENDOR) {
            Vendor vendor = vendorRepository.findByUser_Id(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Vendor profile not found"));
            if (!order.getQuotation().getVendor().getId().equals(vendor.getId())) {
                throw new AccessDeniedException("You are not the vendor for this order");
            }
        } else if (user.getRole().getName() == Role.RoleName.CUSTOMER) {
            if (!order.getQuotation().getRequest().getCustomer().getId().equals(user.getId())) {
                throw new AccessDeniedException("You are not the customer for this order");
            }
        }

        return mapToDTO(order);
    }

    private OrderDTO mapToDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .quotationId(order.getQuotation().getId())
                .requestId(order.getQuotation().getRequest().getId())
                .requestTitle(order.getQuotation().getRequest().getTitle())
                .vendorName(order.getQuotation().getVendor().getCompanyName())
                .customerName(order.getQuotation().getRequest().getCustomer().getFullName())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
