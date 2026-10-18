package com.geli.warehouse;

import com.geli.warehouse.common.exception.InsufficientStockException;
import com.geli.warehouse.dto.request.order.CreateOrderRequest;
import com.geli.warehouse.dto.request.order.OrderItemRequest;
import com.geli.warehouse.dto.response.order.OrderResponse;
import com.geli.warehouse.model.entity.Inventory;
import com.geli.warehouse.model.entity.Item;
import com.geli.warehouse.model.entity.ItemVariant;
import com.geli.warehouse.model.entity.Order;
import com.geli.warehouse.model.enums.ItemStatus;
import com.geli.warehouse.repository.InventoryRepository;
import com.geli.warehouse.repository.ItemVariantRepository;
import com.geli.warehouse.repository.OrderRepository;
import com.geli.warehouse.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemVariantRepository itemVariantRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Item mockItem;
    private ItemVariant mockVariant;
    private Inventory mockInventory;

    @BeforeEach
    void setUp() {
        mockItem = Item.builder()
                .id(1L)
                .sku("ITEM-TSHIRT")
                .name("Classic Cotton T-Shirt")
                .status(ItemStatus.ACTIVE)
                .basePrice(BigDecimal.valueOf(20.00))
                .build();

        mockVariant = ItemVariant.builder()
                .id(10L)
                .item(mockItem)
                .sku("TSHIRT-BLK-M")
                .variantName("Black - M")
                .price(BigDecimal.valueOf(20.00))
                .build();

        mockInventory = Inventory.builder()
                .id(100L)
                .variant(mockVariant)
                .quantity(10)
                .reservedQuantity(0)
                .build();
    }

    @Test
    void createOrder_Success_WhenStockIsSufficient() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .items(Collections.singletonList(
                        OrderItemRequest.builder()
                                .variantId(10L)
                                .quantity(3)
                                .build()
                ))
                .build();

        when(itemVariantRepository.findById(10L)).thenReturn(Optional.of(mockVariant));
        when(inventoryRepository.findByVariantId(10L)).thenReturn(Optional.of(mockInventory));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals("John Doe", response.getCustomerName());
        assertEquals(BigDecimal.valueOf(60.00), response.getTotalAmount());
        assertEquals(7, mockInventory.getQuantity()); // 10 - 3 = 7
        verify(inventoryRepository, times(1)).save(mockInventory);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_ThrowsException_WhenItemIsOutOfStock() {
        mockInventory.setQuantity(0); // Out of stock

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .items(Collections.singletonList(
                        OrderItemRequest.builder()
                                .variantId(10L)
                                .quantity(1)
                                .build()
                ))
                .build();

        when(itemVariantRepository.findById(10L)).thenReturn(Optional.of(mockVariant));
        when(inventoryRepository.findByVariantId(10L)).thenReturn(Optional.of(mockInventory));

        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> orderService.createOrder(request)
        );

        assertTrue(exception.getMessage().contains("OUT OF STOCK"));
        assertEquals(0, mockInventory.getQuantity());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ThrowsException_WhenRequestedExceedsAvailableStock() {
        mockInventory.setQuantity(2); // Only 2 in stock, order wants 5

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .items(Collections.singletonList(
                        OrderItemRequest.builder()
                                .variantId(10L)
                                .quantity(5)
                                .build()
                ))
                .build();

        when(itemVariantRepository.findById(10L)).thenReturn(Optional.of(mockVariant));
        when(inventoryRepository.findByVariantId(10L)).thenReturn(Optional.of(mockInventory));

        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> orderService.createOrder(request)
        );

        assertTrue(exception.getMessage().contains("Requested: 5, Available: 2"));
        assertEquals(2, mockInventory.getQuantity());
        verify(orderRepository, never()).save(any(Order.class));
    }
}
