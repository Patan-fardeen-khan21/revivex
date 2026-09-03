package com.revivex.backend.controller;

import com.revivex.backend.dto.OrderDto;
import com.revivex.backend.entity.InterventionLog;
import com.revivex.backend.repository.InterventionLogRepository;
import com.revivex.backend.repository.UserRepository;
import com.revivex.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final InterventionLogRepository interventionLogRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(Authentication authentication, @RequestBody(required = false) com.revivex.backend.dto.OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrderFromCart(authentication.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getUserOrders(Authentication authentication) {
        return ResponseEntity.ok(orderService.getUserOrders(authentication.getName()));
    }

    @GetMapping("/intervention")
    public ResponseEntity<InterventionLog> getPendingIntervention(Authentication authentication) {
        com.revivex.backend.entity.User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
        return interventionLogRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), "PENDING")
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(authentication.getName(), id));
    }

    @PostMapping("/{id}/apply-discount")
    public ResponseEntity<OrderDto> applyDiscount(Authentication authentication, @PathVariable Long id, @RequestBody com.revivex.backend.dto.OrderRequest request) {
        return ResponseEntity.ok(orderService.applyDiscountToExistingOrder(id, request.getDiscountCode(), authentication.getName()));
    }
}
