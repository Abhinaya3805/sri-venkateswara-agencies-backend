package com.srivenkateswara.agencies.service;

import com.srivenkateswara.agencies.dto.CartDto;
import com.srivenkateswara.agencies.dto.CartItemDto;
import com.srivenkateswara.agencies.dto.CartItemRequest;
import com.srivenkateswara.agencies.entity.Cart;
import com.srivenkateswara.agencies.entity.CartItem;
import com.srivenkateswara.agencies.entity.Product;
import com.srivenkateswara.agencies.entity.User;
import com.srivenkateswara.agencies.exception.BadRequestException;
import com.srivenkateswara.agencies.exception.ResourceNotFoundException;
import com.srivenkateswara.agencies.repository.CartItemRepository;
import com.srivenkateswara.agencies.repository.CartRepository;
import com.srivenkateswara.agencies.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final ProductService productService;

    @Transactional
    public Cart getOrCreateCartForUser(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    @Transactional(readOnly = true)
    public CartDto getCartDtoForCurrentUser() {
        User currentUser = userService.getCurrentlyAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(currentUser);
        return mapToCartDto(cart);
    }

    @Transactional
    public CartDto addItemToCart(CartItemRequest request) {
        User currentUser = userService.getCurrentlyAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(currentUser);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (!product.getActive()) {
            throw new BadRequestException("Product is currently unavailable");
        }

        if (product.getStock() < request.getQuantity()) {
            throw new BadRequestException("Requested quantity exceeds available stock (" + product.getStock() + ")");
        }

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (product.getStock() < newQuantity) {
                throw new BadRequestException("Total requested quantity (" + newQuantity + ") exceeds available stock (" + product.getStock() + ")");
            }
            existingItem.setQuantity(newQuantity);
            existingItem.setUnitPrice(product.getPrice());
            cartItemRepository.save(existingItem);
        } else {
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();
            cartItemRepository.save(cartItem);
        }

        Cart updatedCart = cartRepository.findById(cart.getId()).get();
        return mapToCartDto(updatedCart);
    }

    @Transactional
    public CartDto updateCartItemQuantity(Long cartItemId, Integer quantity) {
        User currentUser = userService.getCurrentlyAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(currentUser);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to your cart");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            Product product = cartItem.getProduct();
            if (product.getStock() < quantity) {
                throw new BadRequestException("Requested quantity (" + quantity + ") exceeds available stock (" + product.getStock() + ")");
            }
            cartItem.setQuantity(quantity);
            cartItem.setUnitPrice(product.getPrice());
            cartItemRepository.save(cartItem);
        }

        Cart updatedCart = cartRepository.findById(cart.getId()).get();
        return mapToCartDto(updatedCart);
    }

    @Transactional
    public CartDto removeCartItem(Long cartItemId) {
        User currentUser = userService.getCurrentlyAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(currentUser);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to your cart");
        }

        cartItemRepository.delete(cartItem);

        Cart updatedCart = cartRepository.findById(cart.getId()).get();
        return mapToCartDto(updatedCart);
    }

    @Transactional
    public CartDto clearCart() {
        User currentUser = userService.getCurrentlyAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(currentUser);

        cartItemRepository.deleteByCartId(cart.getId());
        cart.getItems().clear();

        return mapToCartDto(cart);
    }

    public CartDto mapToCartDto(Cart cart) {
        BigDecimal subtotal = BigDecimal.ZERO;
        List<CartItemDto> itemDtos = new ArrayList<>();

        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                subtotal = subtotal.add(itemTotal);

                itemDtos.add(CartItemDto.builder()
                        .id(item.getId())
                        .product(productService.mapToProductDto(item.getProduct()))
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .itemTotal(itemTotal)
                        .build());
            }
        }

        // Delivery Charge Calculation: Free above ₹500, else ₹40 (if cart has items)
        BigDecimal deliveryCharge = BigDecimal.ZERO;
        if (subtotal.compareTo(BigDecimal.ZERO) > 0 && subtotal.compareTo(new BigDecimal("500")) < 0) {
            deliveryCharge = new BigDecimal("40.00");
        }

        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal total = subtotal.add(deliveryCharge).subtract(discount);

        return CartDto.builder()
                .id(cart.getId())
                .items(itemDtos)
                .subtotal(subtotal)
                .deliveryCharge(deliveryCharge)
                .discount(discount)
                .total(total)
                .build();
    }
}
