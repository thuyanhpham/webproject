package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.Booking;
import com.example.demo.cinema.entity.Payment;
import com.example.demo.cinema.enums.PaymentMethod;
import com.example.demo.cinema.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.Optional;

public interface PaymentService {
    // Phương thức để tạo một Payment mới với trạng thái PENDING
    Payment createPendingPayment(BigDecimal amount, String momoOrderId, String momoRequestId, PaymentMethod method, Booking booking);

    // Phương thức để cập nhật trạng thái Payment sau IPN/Callback
    // Bao gồm cả mã giao dịch, trạng thái, mã lỗi, thông báo, và chữ ký
    Payment updatePaymentStatus(String momoOrderId, String momoTransactionId, PaymentStatus status, Integer resultCode, String message, String signature);
    
    // Phương thức để tìm Payment theo momoOrderId (có thể dùng trong Controller nếu cần)
    Optional<Payment> findByMomoOrderId(String momoOrderId);

    
}