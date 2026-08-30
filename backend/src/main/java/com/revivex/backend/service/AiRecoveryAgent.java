package com.revivex.backend.service;

import com.revivex.backend.entity.Cart;
import com.revivex.backend.entity.Order;
import com.revivex.backend.entity.OrderStatus;
import com.revivex.backend.entity.InterventionLog;
import com.revivex.backend.repository.CartRepository;
import com.revivex.backend.repository.OrderRepository;
import com.revivex.backend.repository.InterventionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRecoveryAgent {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final InterventionLogRepository interventionLogRepository;

    @Scheduled(fixedRate = 60000) // Runs every minute
    @Transactional
    public void detectAndRecoverRevenue() {
        log.info("[AI Recovery Agent] Sweeping for revenue at risk...");

        LocalDateTime now = LocalDateTime.now();

        // 1. Checkout Abandonment (Carts inactive > 2 minutes)
        List<Cart> allCarts = cartRepository.findAll();
        LocalDateTime cartThreshold = now.minusMinutes(2);

        for (Cart cart : allCarts) {
            if (cart.getItems().isEmpty()) continue;

            if (cart.getUpdatedAt() != null && cart.getUpdatedAt().isBefore(cartThreshold)) {
                Optional<InterventionLog> existingLog = interventionLogRepository
                        .findFirstByUserIdAndStatusOrderByCreatedAtDesc(cart.getUser().getId(), "PENDING");

                // Only generate if no active PENDING intervention exists
                if (existingLog.isEmpty()) {
                    BigDecimal totalValue = cart.getItems().stream()
                            .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    String itemsList = cart.getItems().stream()
                            .map(i -> i.getProduct().getName())
                            .collect(Collectors.joining(", "));

                    String discountCode = "RECOVER_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                    BigDecimal riskScore = totalValue.compareTo(new BigDecimal("10000")) > 0 ? new BigDecimal("85.00") : new BigDecimal("60.00");

                    String reasoning = String.format(
                            "[THOUGHT] Cart updated more than 2 minutes ago. Items: [%s]. Total value ₹%.2f. " +
                            "[ANALYSIS] Customer showed high purchase intent but got distracted. Probability of conversion is falling. " +
                            "Category-level analysis indicates moderate price sensitivity. Risk Score: %s%%. " +
                            "[DECISION] Generate 15%% discount code %s to save checkout.",
                            itemsList, totalValue, riskScore, discountCode
                    );

                    InterventionLog logEntry = InterventionLog.builder()
                            .user(cart.getUser())
                            .interventionType("15%_DISCOUNT_OFFER")
                            .discountCode(discountCode)
                            .revenueAtRisk(totalValue)
                            .riskScore(riskScore)
                            .reasoning(reasoning)
                            .status("PENDING")
                            .build();

                    interventionLogRepository.save(logEntry);
                    log.info("[AI Agent] Checkout Abandonment intervention logged for: {}", cart.getUser().getEmail());
                }
            }
        }

        // 2. Payment Failure (Orders marked FAILED in the last 10 minutes)
        List<Order> allOrders = orderRepository.findAll();
        LocalDateTime failureThreshold = now.minusMinutes(10);

        for (Order order : allOrders) {
            if (order.getStatus() == OrderStatus.FAILED && order.getUpdatedAt().isAfter(failureThreshold)) {
                // Check if we already have a pending/recovered log for this failed order
                Optional<InterventionLog> existingLog = interventionLogRepository
                        .findFirstByUserIdAndStatusOrderByCreatedAtDesc(order.getUser().getId(), "PENDING");

                if (existingLog.isEmpty() || !existingLog.get().getInterventionType().equals("PAYMENT_FAILURE_RECOVERY")) {
                    String discountCode = "RETRY10_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                    BigDecimal riskScore = new BigDecimal("90.00"); // Payment failures are extremely high risk

                    String reasoning = String.format(
                            "[THOUGHT] Order #%d failed. Total value ₹%.2f. " +
                            "[ANALYSIS] Transaction failed at payment gateway. User intent is high but blocked by friction. " +
                            "Immediate follow-up is critical. Risk Score: 90.00%%. " +
                            "[DECISION] Generate 10%% payment retry recovery code %s to rescue the order.",
                            order.getId(), order.getTotalAmount(), discountCode
                    );

                    InterventionLog logEntry = InterventionLog.builder()
                            .user(order.getUser())
                            .interventionType("PAYMENT_FAILURE_RECOVERY")
                            .discountCode(discountCode)
                            .revenueAtRisk(order.getTotalAmount())
                            .riskScore(riskScore)
                            .reasoning(reasoning)
                            .status("PENDING")
                            .build();

                    interventionLogRepository.save(logEntry);
                    log.info("[AI Agent] Payment Failure intervention logged for Order #{}", order.getId());
                }
            }
        }

        // 3. Overdue Receivables (Orders marked PENDING > 5 minutes ago)
        LocalDateTime overdueThreshold = now.minusMinutes(5);

        for (Order order : allOrders) {
            if (order.getStatus() == OrderStatus.PENDING && order.getCreatedAt().isBefore(overdueThreshold)) {
                // Check if we already have a log for this overdue order
                Optional<InterventionLog> existingLog = interventionLogRepository
                        .findFirstByUserIdAndStatusOrderByCreatedAtDesc(order.getUser().getId(), "PENDING");

                if (existingLog.isEmpty()) {
                    String discountCode = "RECV5_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                    BigDecimal riskScore = order.getTotalAmount().compareTo(new BigDecimal("20000")) > 0 
                            ? new BigDecimal("75.00") : new BigDecimal("50.00");

                    String reasoning = String.format(
                            "[THOUGHT] Placed Order #%d is unpaid for over 5 minutes. " +
                            "[ANALYSIS] Unpaid B2B/B2C receivable detected. Total invoice value: ₹%.2f. " +
                            "Billing hesitation or settlement friction suspected. Risk Score: %s%%. " +
                            "[DECISION] Issue overdue invoice reminder with a 5%% early-payment settlement incentive %s.",
                            order.getId(), order.getTotalAmount(), riskScore, discountCode
                    );

                    InterventionLog logEntry = InterventionLog.builder()
                            .user(order.getUser())
                            .interventionType("OVERDUE_RECEIVABLE_REMINDER")
                            .discountCode(discountCode)
                            .revenueAtRisk(order.getTotalAmount())
                            .riskScore(riskScore)
                            .reasoning(reasoning)
                            .status("PENDING")
                            .build();

                    interventionLogRepository.save(logEntry);
                    log.info("[AI Agent] Overdue Receivable reminder logged for Order #{}", order.getId());
                }
            }
        }
    }
}
