package com.revivex.backend.service;


import com.revivex.backend.dto.OrderDto;
import com.revivex.backend.entity.*;
import com.revivex.backend.repository.CartRepository;
import com.revivex.backend.repository.InterventionLogRepository;
import com.revivex.backend.repository.OrderRepository;
import com.revivex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final InterventionLogRepository interventionLogRepository;

    @Transactional
    public OrderDto createOrderFromCart(String email, com.revivex.backend.dto.OrderRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        BigDecimal totalAmount = cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Apply AI Discount if provided
        if (request != null && request.getDiscountCode() != null) {
            java.util.Optional<InterventionLog> logOpt = interventionLogRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), "PENDING");
            
            if (logOpt.isPresent() && logOpt.get().getDiscountCode().equals(request.getDiscountCode())) {
                // Apply 15% discount
                BigDecimal discount = totalAmount.multiply(new BigDecimal("0.15"));
                totalAmount = totalAmount.subtract(discount);
                
                // Mark as recovered
                InterventionLog log = logOpt.get();
                log.setStatus("RECOVERED");
                log.setRecoveredAt(java.time.LocalDateTime.now());
                interventionLogRepository.save(log);
            }
        }

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .build();

        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> OrderItem.builder()
                .order(order)
                .product(cartItem.getProduct())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getProduct().getPrice())
                .build()).collect(Collectors.toList());

        order.setItems(orderItems);

        order.setRazorpayOrderId("order_MOCK_" + System.currentTimeMillis());

        Order savedOrder = orderRepository.save(order);
        
        // Clear cart after order creation
        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToDto(savedOrder);
    }

    public List<OrderDto> getUserOrders(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public OrderDto getOrderById(String email, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized to view this order");
        }
        return mapToDto(order);
    }

    @Transactional
    public OrderDto applyDiscountToExistingOrder(Long orderId, String discountCode, String email) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        java.util.Optional<InterventionLog> logOpt = interventionLogRepository
            .findFirstByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), "PENDING");
            
        if (logOpt.isPresent() && logOpt.get().getDiscountCode().equals(discountCode)) {
            InterventionLog log = logOpt.get();
            
            BigDecimal discountPercentage = BigDecimal.ZERO;
            if (log.getInterventionType().equals("PAYMENT_FAILURE_RECOVERY")) {
                discountPercentage = new BigDecimal("0.10"); // 10%
            } else if (log.getInterventionType().equals("OVERDUE_RECEIVABLE_REMINDER")) {
                discountPercentage = new BigDecimal("0.05"); // 5%
            }
            
            if (discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountAmount = order.getTotalAmount().multiply(discountPercentage);
                order.setTotalAmount(order.getTotalAmount().subtract(discountAmount));
                
                log.setStatus("RECOVERED");
                log.setRecoveredAt(java.time.LocalDateTime.now());
                interventionLogRepository.save(log);
                
                order = orderRepository.save(order);
            }
        }
        
        return mapToDto(order);
    }

    private OrderDto mapToDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .razorpayOrderId(order.getRazorpayOrderId())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
