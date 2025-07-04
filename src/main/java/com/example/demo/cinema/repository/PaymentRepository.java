package com.example.demo.cinema.repository; // Đảm bảo package này đúng

import com.example.demo.cinema.entity.Payment; // Import Payment entity
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Thêm phương thức tìm kiếm Payment bằng momoOrderId (rất quan trọng cho IPN)
    Optional<Payment> findByMomoOrderId(String momoOrderId);
    Optional<Payment> findByMomoRequestId(String momoRequestId); // Tùy chọn, nếu bạn dùng requestId để tìm kiếm
}