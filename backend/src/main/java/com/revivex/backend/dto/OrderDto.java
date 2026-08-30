package com.revivex.backend.dto;

import com.revivex.backend.entity.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderDto {
    private Long id;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String razorpayOrderId;
    private LocalDateTime createdAt;
}
