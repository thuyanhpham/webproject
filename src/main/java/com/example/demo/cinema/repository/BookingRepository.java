package com.example.demo.cinema.repository; 

import com.example.demo.cinema.entity.Booking;
import com.example.demo.cinema.entity.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; 

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
	List<Booking> findByUser(User user); // ✅ Dùng cho show history
    // List<Booking> findByUser(User user);
    // Optional<Booking> findByMomoOrderId(String momoOrderId); // Nếu bạn muốn tìm Booking bằng momoOrderId
	List<Booking> findByUserOrderByBookingTimeDesc(User user);
}