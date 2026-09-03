package com.revivex.backend.config;

import com.revivex.backend.entity.InterventionLog;
import com.revivex.backend.entity.Product;
import com.revivex.backend.entity.Role;
import com.revivex.backend.entity.User;
import com.revivex.backend.repository.InterventionLogRepository;
import com.revivex.backend.repository.ProductRepository;
import com.revivex.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final InterventionLogRepository logRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail("fardeenkhanpatan5@gmail.com")) {
            userRepository.save(User.builder()
                    .name("Fardeen Khan")
                    .email("fardeenkhanpatan5@gmail.com")
                    .password(passwordEncoder.encode("password"))
                    .role(Role.ADMIN)
                    .build());
        }

        if (!userRepository.existsByEmail("admin@revivex.com")) {
            userRepository.save(User.builder()
                    .name("Admin User")
                    .email("admin@revivex.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build());
        }

        if (productRepository.count() == 0) {
            Product p1 = Product.builder()
                    .name("Premium Wireless Headphones")
                    .description("High-quality noise-canceling wireless headphones.")
                    .price(new BigDecimal("299.99"))
                    .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&q=80")
                    .build();

            Product p2 = Product.builder()
                    .name("Mechanical Keyboard")
                    .description("RGB Mechanical Keyboard with Cherry MX Red switches.")
                    .price(new BigDecimal("129.50"))
                    .imageUrl("https://images.unsplash.com/photo-1595225476474-87563907a212?w=800&q=80")
                    .build();

            Product p3 = Product.builder()
                    .name("4K Gaming Monitor")
                    .description("27-inch 4K UHD gaming monitor with 144Hz refresh rate.")
                    .price(new BigDecimal("499.00"))
                    .imageUrl("https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=800&q=80")
                    .build();
            
            Product p4 = Product.builder()
                    .name("Ergonomic Mouse")
                    .description("Wireless ergonomic mouse for comfortable all-day use.")
                    .price(new BigDecimal("79.99"))
                    .imageUrl("https://images.unsplash.com/photo-1527814050087-379381547969?w=800&q=80")
                    .build();

            productRepository.saveAll(Arrays.asList(p1, p2, p3, p4));
        }

        if (logRepository.count() == 0) {
            System.out.println("[DataSeeder] Populating hackathon demo data...");
            
            User dummy1 = userRepository.findByEmail("customer1@example.com").orElseGet(() -> {
                return userRepository.save(User.builder().name("Rohit Sharma").email("customer1@example.com").password("mock").role(Role.USER).build());
            });
            User dummy2 = userRepository.findByEmail("customer2@example.com").orElseGet(() -> {
                return userRepository.save(User.builder().name("Priya Patel").email("customer2@example.com").password("mock").role(Role.USER).build());
            });
            User dummy3 = userRepository.findByEmail("customer3@example.com").orElseGet(() -> {
                return userRepository.save(User.builder().name("Amit Singh").email("customer3@example.com").password("mock").role(Role.USER).build());
            });

            InterventionLog log1 = InterventionLog.builder()
                    .user(dummy1)
                    .interventionType("DISCOUNT_OFFER")
                    .discountCode("RECOVER15_A1")
                    .revenueAtRisk(new BigDecimal("12500.00"))
                    .status("RECOVERED")
                    .createdAt(LocalDateTime.now().minusDays(2))
                    .recoveredAt(LocalDateTime.now().minusDays(2).plusHours(1))
                    .build();

            InterventionLog log2 = InterventionLog.builder()
                    .user(dummy2)
                    .interventionType("DISCOUNT_OFFER")
                    .discountCode("RECOVER15_B2")
                    .revenueAtRisk(new BigDecimal("8400.00"))
                    .status("RECOVERED")
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .recoveredAt(LocalDateTime.now().minusDays(1).plusMinutes(15))
                    .build();

            InterventionLog log3 = InterventionLog.builder()
                    .user(dummy3)
                    .interventionType("REMINDER_EMAIL")
                    .discountCode("N/A")
                    .revenueAtRisk(new BigDecimal("4200.00"))
                    .status("PENDING")
                    .createdAt(LocalDateTime.now().minusHours(5))
                    .build();

            InterventionLog log4 = InterventionLog.builder()
                    .user(dummy1)
                    .interventionType("DISCOUNT_OFFER")
                    .discountCode("RECOVER15_C3")
                    .revenueAtRisk(new BigDecimal("15000.00"))
                    .status("RECOVERED")
                    .createdAt(LocalDateTime.now().minusHours(2))
                    .recoveredAt(LocalDateTime.now().minusHours(1))
                    .build();

            InterventionLog log5 = InterventionLog.builder()
                    .user(dummy2)
                    .interventionType("DISCOUNT_OFFER")
                    .discountCode("RECOVER15_D4")
                    .revenueAtRisk(new BigDecimal("6800.00"))
                    .status("PENDING")
                    .createdAt(LocalDateTime.now().minusMinutes(30))
                    .build();

            logRepository.saveAll(List.of(log1, log2, log3, log4, log5));
        }
    }
}
