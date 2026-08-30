package com.revivex.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "intervention_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterventionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String interventionType; // e.g., "DISCOUNT_OFFER", "REMINDER_EMAIL"

    @Column(nullable = false)
    private String discountCode; // e.g., "RECOVER15"

    private BigDecimal revenueAtRisk; // Total value of the abandoned cart

    @Column(nullable = false)
    private String status; // e.g., "PENDING", "RECOVERED", "IGNORED"

    @Column(precision = 5, scale = 2)
    private BigDecimal riskScore;

    @Column(length = 2000)
    private String reasoning;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime recoveredAt;
}
