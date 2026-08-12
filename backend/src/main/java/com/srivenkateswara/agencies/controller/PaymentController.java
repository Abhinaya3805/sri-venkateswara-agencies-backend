package com.srivenkateswara.agencies.controller;

import com.srivenkateswara.agencies.dto.ApiResponse;
import com.srivenkateswara.agencies.dto.PaymentDto;
import com.srivenkateswara.agencies.dto.PaymentRequest;
import com.srivenkateswara.agencies.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment tracking and processing endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment info for an order")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentByOrderId(@PathVariable Long orderId) {
        PaymentDto payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payment info retrieved", payment));
    }

    @PostMapping("/process")
    @Operation(summary = "Process or update payment for an order")
    public ResponseEntity<ApiResponse<PaymentDto>> processPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentDto payment = paymentService.processPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", payment));
    }
}
