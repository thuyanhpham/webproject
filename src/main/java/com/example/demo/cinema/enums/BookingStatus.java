package com.example.demo.cinema.enums;

public enum BookingStatus {
	PENDING,        // Đặt chỗ đã tạo nhưng chưa thanh toán
    CONFIRMED,      // Đặt chỗ đã được xác nhận (thanh toán thành công)
    CANCELLED,      // Đặt chỗ bị hủy
    PAYMENT_FAILED  // Thanh toán thất bại cho đặt chỗ này
}
