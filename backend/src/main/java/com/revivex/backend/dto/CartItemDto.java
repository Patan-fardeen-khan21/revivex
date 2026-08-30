package com.revivex.backend.dto;

import com.revivex.backend.entity.Product;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemDto {
    private Long id;
    private Product product;
    private Integer quantity;
}
