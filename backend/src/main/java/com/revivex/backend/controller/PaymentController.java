package com.revivex.backend.controller;

import com.revivex.backend.dto.PaymentVerifyRequest;
import com.revivex.backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyPayment(@Valid @RequestBody PaymentVerifyRequest request) {
        boolean isValid = paymentService.verifyPayment(request);
        Map<String, String> response = new HashMap<>();
        
        if (isValid) {
            response.put("status", "success");
            response.put("message", "Payment verified successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "failed");
            response.put("message", "Payment verification failed");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/fail")
    public ResponseEntity<Map<String, String>> failPayment(@Valid @RequestBody PaymentVerifyRequest request) {
        boolean isProcessed = paymentService.failPayment(request);
        Map<String, String> response = new HashMap<>();
        response.put("status", "failed_recorded");
        response.put("message", "Payment failure recorded successfully");
        return ResponseEntity.ok(response);
    }
}
