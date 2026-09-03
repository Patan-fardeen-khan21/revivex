package com.revivex.backend.service;

import com.razorpay.Utils;
import com.revivex.backend.dto.PaymentVerifyRequest;
import com.revivex.backend.entity.Order;
import com.revivex.backend.entity.OrderStatus;
import com.revivex.backend.entity.Payment;
import com.revivex.backend.entity.PaymentStatus;
import com.revivex.backend.repository.OrderRepository;
import com.revivex.backend.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    @Transactional
    public boolean verifyPayment(PaymentVerifyRequest request) {
        Order order = null;
        if (request.getRazorpayOrderId() != null && !request.getRazorpayOrderId().isBlank()) {
            order = orderRepository.findByRazorpayOrderId(request.getRazorpayOrderId()).orElse(null);
        }
        if (order == null) {
            order = orderRepository.findFirstByOrderByCreatedAtDesc()
                    .orElseThrow(() -> new RuntimeException("Order not found"));
        }

        try {
            boolean isValid = true; // Mocked for hackathon

            if (isValid) {
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);

                Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
                if (payment == null) {
                    payment = Payment.builder().order(order).build();
                }

                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
                payment.setRazorpaySignature(request.getRazorpaySignature());
                payment.setAmount(order.getTotalAmount());
                paymentRepository.save(payment);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error verifying signature: " + e.getMessage());
        }
    }

    @Transactional
    public boolean failPayment(PaymentVerifyRequest request) {
        Order order = null;
        if (request.getRazorpayOrderId() != null && !request.getRazorpayOrderId().isBlank()) {
            order = orderRepository.findByRazorpayOrderId(request.getRazorpayOrderId()).orElse(null);
        }
        if (order == null) {
            order = orderRepository.findFirstByOrderByCreatedAtDesc()
                    .orElseThrow(() -> new RuntimeException("Order not found"));
        }

        order.setStatus(OrderStatus.FAILED);
        orderRepository.save(order);

        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        if (payment == null) {
            payment = Payment.builder().order(order).build();
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setAmount(order.getTotalAmount());
        paymentRepository.save(payment);
        return true;
    }
}
