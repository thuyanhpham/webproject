package com.example.demo.cinema.service; // Đảm bảo package này đúng

import com.example.demo.cinema.entity.Booking;
import com.example.demo.cinema.entity.User;

import java.util.List;
import java.util.Optional;

public interface BookingService {
    Booking save(Booking booking);
    Booking saveAndFlush(Booking booking); 
    Optional<Booking> findById(Long id);
	List<Booking> findBookingsByUser(User currentUser);
    
}