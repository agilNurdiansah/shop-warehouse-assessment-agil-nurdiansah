package com.geli.warehouse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geli.warehouse.dto.request.order.CreateOrderRequest;
import com.geli.warehouse.dto.request.order.OrderItemRequest;
import com.geli.warehouse.model.entity.ItemVariant;
import com.geli.warehouse.repository.ItemVariantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemVariantRepository itemVariantRepository;

    @Test
    void testPlaceOrder_Success_WhenVariantInStock() throws Exception {
        ItemVariant inStockVariant = itemVariantRepository.findBySku("TSHIRT-BLK-M")
                .orElseThrow();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("Alice Smith")
                .customerEmail("alice@example.com")
                .items(Collections.singletonList(
                        OrderItemRequest.builder()
                                .variantId(inStockVariant.getId())
                                .quantity(2)
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.customerName").value("Alice Smith"))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].variantSku").value("TSHIRT-BLK-M"));
    }

    @Test
    void testPlaceOrder_Rejected_WhenVariantOutOfStock() throws Exception {
        // TSHIRT-WHT-XL was initialized with stock 0 in DataInitializer
        ItemVariant outOfStockVariant = itemVariantRepository.findBySku("TSHIRT-WHT-XL")
                .orElseThrow();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("Bob Miller")
                .customerEmail("bob@example.com")
                .items(Collections.singletonList(
                        OrderItemRequest.builder()
                                .variantId(outOfStockVariant.getId())
                                .quantity(1)
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.status").value("gagal"))
                .andExpect(jsonPath("$.message", containsString("OUT OF STOCK")));
    }
}
