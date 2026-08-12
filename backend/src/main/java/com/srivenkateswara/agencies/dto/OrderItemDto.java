package com.srivenkateswara.agencies.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDto {

    private Long id;
    private ProductDto product;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}
