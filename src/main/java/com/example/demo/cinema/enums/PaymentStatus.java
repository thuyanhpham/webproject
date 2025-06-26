package com.example.demo.cinema.enums;

public enum PaymentStatus {

	PENDING,    // Đang chờ thanh toán (Request sent to Momo, waiting for result)
    SUCCESS,    // Thanh toán thành công (Confirmed by Momo IPN)
    FAILED,     // Thanh toán thất bại (Confirmed by Momo IPN, hoặc lỗi khởi tạo)
    CANCELLED,  // Thanh toán bị hủy bởi người dùng hoặc hệ thống
    REFUNDED,   // Đã hoàn tiền
    EXPIRED     // Hết thời gian thanh toán
}
