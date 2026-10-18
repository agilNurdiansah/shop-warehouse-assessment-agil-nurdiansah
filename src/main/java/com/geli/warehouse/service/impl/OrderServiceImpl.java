package com.geli.warehouse.service.impl;

import com.geli.warehouse.common.exception.BadRequestException;
import com.geli.warehouse.common.exception.InsufficientStockException;
import com.geli.warehouse.common.exception.ResourceNotFoundException;
import com.geli.warehouse.dto.request.order.CreateOrderRequest;
import com.geli.warehouse.dto.request.order.OrderItemRequest;
import com.geli.warehouse.dto.response.order.OrderItemResponse;
import com.geli.warehouse.dto.response.order.OrderResponse;
import com.geli.warehouse.model.entity.Inventory;
import com.geli.warehouse.model.entity.ItemVariant;
import com.geli.warehouse.model.entity.Order;
import com.geli.warehouse.model.entity.OrderItem;
import com.geli.warehouse.model.enums.ItemStatus;
import com.geli.warehouse.model.enums.OrderStatus;
import com.geli.warehouse.repository.InventoryRepository;
import com.geli.warehouse.repository.ItemVariantRepository;
import com.geli.warehouse.repository.OrderRepository;
import com.geli.warehouse.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemVariantRepository itemVariantRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customerName(request.getCustomerName().trim())
                .customerEmail(request.getCustomerEmail().trim().toLowerCase())
                .status(OrderStatus.COMPLETED)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            ItemVariant variant = itemVariantRepository.findById(itemReq.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item variant not found with id: " + itemReq.getVariantId()));

            if (variant.getItem() != null && variant.getItem().getStatus() != ItemStatus.ACTIVE) {
                throw new BadRequestException("Cannot purchase variant '" + variant.getSku() + "' because the item is inactive");
            }

            Inventory inventory = inventoryRepository.findByVariantId(variant.getId())
                    .orElseThrow(() -> new InsufficientStockException("Inventory not initialized for variant: " + variant.getSku()));

            int availableStock = inventory.getAvailableQuantity();
            int requestedQuantity = itemReq.getQuantity();

            // Prevent selling out-of-stock items
            if (availableStock < requestedQuantity) {
                String stockStatus = availableStock == 0 ? " (OUT OF STOCK)" : "";
                throw new InsufficientStockException(
                        "Cannot fulfill order for variant '" + variant.getVariantName() +
                        "' [SKU: " + variant.getSku() + "]" + stockStatus +
                        ". Requested: " + requestedQuantity + ", Available: " + availableStock
                );
            }

            // Deduct stock atomically
            inventory.setQuantity(inventory.getQuantity() - requestedQuantity);
            inventoryRepository.save(inventory);

            BigDecimal unitPrice = variant.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(requestedQuantity));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .variant(variant)
                    .variantSku(variant.getSku())
                    .variantName(variant.getVariantName())
                    .unitPrice(unitPrice)
                    .quantity(requestedQuantity)
                    .subtotal(subtotal)
                    .build();

            order.addItem(orderItem);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));
        return mapToOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .variantId(item.getVariant().getId())
                        .variantSku(item.getVariantSku())
                        .variantName(item.getVariantName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
