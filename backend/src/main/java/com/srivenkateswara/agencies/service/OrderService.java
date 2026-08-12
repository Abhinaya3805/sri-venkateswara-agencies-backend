package com.srivenkateswara.agencies.service;

import com.srivenkateswara.agencies.dto.*;
import com.srivenkateswara.agencies.entity.*;
import com.srivenkateswara.agencies.exception.BadRequestException;
import com.srivenkateswara.agencies.exception.ResourceNotFoundException;
import com.srivenkateswara.agencies.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final UserService userService;
    private final AddressService addressService;
    private final ProductService productService;
    private final AuthService authService;

    @Transactional
    public OrderDto createOrder(OrderRequest request) {
        User currentUser = userService.getCurrentlyAuthenticatedUser();

        Address address = addressRepository.findByIdAndUserId(request.getAddressId(), currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", request.getAddressId()));

        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new BadRequestException("Cart is empty or not found"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot place order with an empty cart");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // Generate Order Number: SVA-YYYYMMDD-XXXX
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomSuffix = String.format("%04d", new Random().nextInt(10000));
        String orderNumber = "SVA-" + datePrefix + "-" + randomSuffix;

        // Verify stock & calculate totals
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (!product.getActive()) {
                throw new BadRequestException("Product '" + product.getName() + "' is no longer available");
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for '" + product.getName() + "'. Available: " + product.getStock());
            }

            // Deduct stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            BigDecimal itemSubtotal = cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);
        }

        BigDecimal deliveryCharge = subtotal.compareTo(new BigDecimal("500")) >= 0 ? BigDecimal.ZERO : new BigDecimal("40.00");
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.add(deliveryCharge).subtract(discount);

        PaymentStatus initialPaymentStatus = request.getPaymentMethod() == PaymentMethod.COD ?
                PaymentStatus.PENDING : PaymentStatus.PENDING;

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(currentUser)
                .address(address)
                .subtotal(subtotal)
                .deliveryCharge(deliveryCharge)
                .discount(discount)
                .totalAmount(totalAmount)
                .paymentStatus(initialPaymentStatus)
                .orderStatus(OrderStatus.PLACED)
                .paymentMethod(request.getPaymentMethod())
                .build();

        Order savedOrder = orderRepository.save(order);

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getUnitPrice())
                    .subtotal(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                    .build();
            orderItemRepository.save(orderItem);
            orderItems.add(orderItem);
        }

        savedOrder.setItems(orderItems);

        // Record initial Payment
        Payment payment = Payment.builder()
                .order(savedOrder)
                .amount(totalAmount)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(initialPaymentStatus)
                .transactionId(request.getPaymentMethod() == PaymentMethod.COD ? "COD-" + orderNumber : "TXN-" + System.currentTimeMillis())
                .build();
        paymentRepository.save(payment);

        // Clear User Cart
        cartItemRepository.deleteByCartId(cart.getId());

        return mapToOrderDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getUserOrders() {
        User currentUser = userService.getCurrentlyAuthenticatedUser();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId()).stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        User currentUser = userService.getCurrentlyAuthenticatedUser();
        Order order = orderRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return mapToOrderDto(order);
    }

    @Transactional
    public OrderDto cancelOrder(Long id) {
        User currentUser = userService.getCurrentlyAuthenticatedUser();
        Order order = orderRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        if (order.getOrderStatus() == OrderStatus.DELIVERED || order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order cannot be cancelled in its current state (" + order.getOrderStatus() + ")");
        }

        // Restore stock
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);
        return mapToOrderDto(updatedOrder);
    }

    // ADMIN APIS
    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrdersForAdmin() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto updateOrderStatusByAdmin(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        order.setOrderStatus(newStatus);
        if (newStatus == OrderStatus.DELIVERED && order.getPaymentMethod() == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.SUCCESS);
            paymentRepository.findByOrderId(order.getId()).ifPresent(payment -> {
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                paymentRepository.save(payment);
            });
        }

        Order updatedOrder = orderRepository.save(order);
        return mapToOrderDto(updatedOrder);
    }

    public OrderDto mapToOrderDto(Order order) {
        List<OrderItemDto> itemDtos = new ArrayList<>();
        if (order.getItems() != null) {
            itemDtos = order.getItems().stream()
                    .map(item -> OrderItemDto.builder()
                            .id(item.getId())
                            .product(productService.mapToProductDto(item.getProduct()))
                            .quantity(item.getQuantity())
                            .price(item.getPrice())
                            .subtotal(item.getSubtotal())
                            .build())
                    .collect(Collectors.toList());
        }

        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .user(authService.mapToUserDto(order.getUser()))
                .address(addressService.mapToAddressDto(order.getAddress()))
                .subtotal(order.getSubtotal())
                .deliveryCharge(order.getDeliveryCharge() != null ? order.getDeliveryCharge() : BigDecimal.ZERO)
                .discount(order.getDiscount())
                .totalAmount(order.getTotalAmount())
                .paymentStatus(order.getPaymentStatus())
                .orderStatus(order.getOrderStatus())
                .paymentMethod(order.getPaymentMethod())
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
