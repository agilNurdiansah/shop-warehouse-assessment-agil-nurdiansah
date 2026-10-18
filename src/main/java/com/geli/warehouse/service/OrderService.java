package com.geli.warehouse.service;

import com.geli.warehouse.dto.request.order.CreateOrderRequest;
import com.geli.warehouse.dto.response.order.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse getOrderById(Long id);
    OrderResponse getOrderByNumber(String orderNumber);
    List<OrderResponse> getAllOrders();
}
