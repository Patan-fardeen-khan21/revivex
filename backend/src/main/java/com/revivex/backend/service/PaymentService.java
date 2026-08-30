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
        Order order = orderRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        try {
            boolean isValid = true; // Mocked for hackathon

            if (isValid) {
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);

                Payment payment = Payment.builder()
                        .order(order)
                        .status(PaymentStatus.SUCCESS)
                        .razorpayPaymentId(request.getRazorpayPaymentId())
                        .razorpaySignature(request.getRazorpaySignature())
                        .amount(order.getTotalAmount())
                        .build();
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
        Order order = orderRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.FAILED);
        orderRepository.save(order);

        Payment payment = Payment.builder()
                .order(order)
                .status(PaymentStatus.FAILED)
                .razorpayPaymentId(request.getRazorpayPaymentId())
                .razorpaySignature(request.getRazorpaySignature())
                .amount(order.getTotalAmount())
                .build();
        paymentRepository.save(payment);
        return true;
    }
}
