package com.srivenkateswara.agencies.dto;

import com.srivenkateswara.agencies.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {

    private Long id;
    private String name;
    private String brand;
    private String description;
    private BigDecimal price;
    private String packSize;
    private Integer stock;
    private String imageUrl;
    private Boolean active;
    private Boolean featured;
    private Category category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
