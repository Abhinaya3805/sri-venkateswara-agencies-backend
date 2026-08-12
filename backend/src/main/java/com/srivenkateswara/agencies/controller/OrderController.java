package com.srivenkateswara.agencies.controller;

import com.srivenkateswara.agencies.dto.ApiResponse;
import com.srivenkateswara.agencies.dto.OrderDto;
import com.srivenkateswara.agencies.dto.OrderRequest;
import com.srivenkateswara.agencies.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Customer order placement, tracking, and history endpoints")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place a new beverage order")
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderDto order = orderService.createOrder(request);
        return new ResponseEntity<>(ApiResponse.success("Order placed successfully", order), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get order history for logged-in user")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getUserOrders() {
        List<OrderDto> orders = orderService.getUserOrders();
        return ResponseEntity.ok(ApiResponse.success("Order history retrieved successfully", orders));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order details by ID")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(@PathVariable Long id) {
        OrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Order details retrieved", order));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel an active order")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(@PathVariable Long id) {
        OrderDto order = orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }
}
