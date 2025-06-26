package com.example.demo.cinema.service.impl; // Đảm bảo package này đúng

import com.example.demo.cinema.entity.Booking;
import com.example.demo.cinema.entity.Payment;
import com.example.demo.cinema.enums.BookingStatus; // Import BookingStatus
import com.example.demo.cinema.enums.PaymentMethod;
import com.example.demo.cinema.enums.PaymentStatus;
import com.example.demo.cinema.repository.BookingRepository; // Cần BookingRepository để cập nhật Booking
import com.example.demo.cinema.repository.PaymentRepository;
import com.example.demo.cinema.service.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Quan trọng cho Transaction

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PaymentServiceImp implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepo;
    @Autowired
    private BookingRepository bookingRepo; 

    @Override
    @Transactional // Đảm bảo toàn bộ hoạt động (lưu Payment và Booking) 
    public Payment createPendingPayment(BigDecimal amount, String momoOrderId, String momoRequestId, PaymentMethod method, Booking booking) {
        Payment payment = new Payment(momoOrderId, momoRequestId, amount, method, booking);
        return paymentRepo.save(payment);
    }

    @Override
    @Transactional
    public Payment updatePaymentStatus(String momoOrderId, String momoTransactionId, PaymentStatus status, Integer resultCode, String message, String signature) {
        Payment payment = paymentRepo.findByMomoOrderId(momoOrderId) 
                .orElseThrow(() -> new RuntimeException("Payment not found for Momo Order ID: " + momoOrderId)); 

        payment.setMomoTransactionId(momoTransactionId);
        payment.setStatus(status);
        payment.setMomoResultCode(resultCode);
        payment.setMomoMessage(message);
        payment.setMomoSignature(signature);
        payment.setUpdatedAt(LocalDateTime.now());

        // Cập nhật trạng thái Booking liên quan
        if (payment.getBooking() != null) {
            Booking booking = payment.getBooking();
            if (status == PaymentStatus.SUCCESS) {
                booking.setStatus(BookingStatus.CONFIRMED); 
            } else if (status == PaymentStatus.FAILED || status == PaymentStatus.CANCELLED || status == PaymentStatus.EXPIRED) {
                booking.setStatus(BookingStatus.PAYMENT_FAILED); 
            }
            bookingRepo.save(booking); // Lưu Booking đã cập nhật
        }
        return paymentRepo.save(payment);
    }

    @Override
    public Optional<Payment> findByMomoOrderId(String momoOrderId) {
        return paymentRepo.findByMomoOrderId(momoOrderId);
    }
}