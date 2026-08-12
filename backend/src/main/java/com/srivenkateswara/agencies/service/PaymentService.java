package com.srivenkateswara.agencies.service;

import com.srivenkateswara.agencies.dto.PaymentDto;
import com.srivenkateswara.agencies.dto.PaymentRequest;
import com.srivenkateswara.agencies.entity.Order;
import com.srivenkateswara.agencies.entity.Payment;
import com.srivenkateswara.agencies.entity.PaymentStatus;
import com.srivenkateswara.agencies.exception.BadRequestException;
import com.srivenkateswara.agencies.exception.ResourceNotFoundException;
import com.srivenkateswara.agencies.repository.OrderRepository;
import com.srivenkateswara.agencies.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public PaymentDto getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return mapToPaymentDto(payment);
    }

    @Transactional
    public PaymentDto processPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseGet(() -> Payment.builder()
                        .order(order)
                        .amount(order.getTotalAmount())
                        .paymentMethod(request.getPaymentMethod())
                        .build());

        payment.setPaymentMethod(request.getPaymentMethod());
        
        if (request.getPaymentStatus() != null) {
            payment.setPaymentStatus(request.getPaymentStatus());
            order.setPaymentStatus(request.getPaymentStatus());
        }

        if (request.getTransactionId() != null) {
            payment.setTransactionId(request.getTransactionId());
        }

        Payment savedPayment = paymentRepository.save(payment);
        orderRepository.save(order);

        return mapToPaymentDto(savedPayment);
    }

    public PaymentDto mapToPaymentDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .orderNumber(payment.getOrder().getOrderNumber())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
