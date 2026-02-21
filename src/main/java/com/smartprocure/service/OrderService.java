package com.smartprocure.service;

import com.smartprocure.dto.OrderDTO;
import com.smartprocure.entity.OrderStatus;
import com.smartprocure.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDTO createOrder(Long quotationId);

    OrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus, User user);

    Page<OrderDTO> getCustomerOrders(User customer, OrderStatus status, String search, Pageable pageable);

    Page<OrderDTO> getVendorOrders(User vendorUser, OrderStatus status, String search, Pageable pageable);

    OrderDTO getOrderById(Long orderId, User user);
}
