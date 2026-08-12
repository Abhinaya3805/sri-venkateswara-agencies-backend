package com.srivenkateswara.agencies.controller;

import com.srivenkateswara.agencies.dto.*;
import com.srivenkateswara.agencies.service.OrderService;
import com.srivenkateswara.agencies.service.ProductService;
import com.srivenkateswara.agencies.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Admin administration endpoints for users, orders, and products")
public class AdminController {

    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;

    @GetMapping("/users")
    @Operation(summary = "Get all registered customers and admins")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @PutMapping("/users/{id}/role")
    @Operation(summary = "Update user roles (e.g. promote user to ADMIN)")
    public ResponseEntity<ApiResponse<UserDto>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleUpdateRequest request) {
        UserDto user = userService.updateUserRole(id, request);
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", user));
    }

    @GetMapping("/orders")
    @Operation(summary = "View all wholesale and customer orders")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getAllOrders() {
        List<OrderDto> orders = orderService.getAllOrdersForAdmin();
        return ResponseEntity.ok(ApiResponse.success("All orders retrieved successfully", orders));
    }

    @PutMapping("/orders/{id}/status")
    @Operation(summary = "Update order status (PLACED, CONFIRMED, PACKED, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED)")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        OrderDto order = orderService.updateOrderStatusByAdmin(id, request.getOrderStatus());
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }

    @GetMapping("/products")
    @Operation(summary = "View all products including inactive ones")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<ProductDto> products = productService.filterProducts(
                null, null, null, null, null, null, page, size, "id", "asc"
        );
        return ResponseEntity.ok(ApiResponse.success("All products retrieved", products));
    }
}
