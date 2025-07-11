package com.example.demo.cinema.service.impl; 

import com.example.demo.cinema.entity.Booking;
import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.enums.BookingStatus;
import com.example.demo.cinema.repository.BookingRepository; 
import com.example.demo.cinema.service.BookingService; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImp implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Override
    @Transactional
    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id); 
    }

   
    @Override
    public List<Booking> findBookingsByUser(User currentUser) {
        // THAY ĐỔI DÒNG NÀY:
        // Gọi phương thức mới để chỉ lấy các đơn hàng có trạng thái CONFIRMED.
        return bookingRepository.findByUserAndStatusOrderByBookingTimeDesc(currentUser, BookingStatus.CONFIRMED);
    }
    @Override
    public Booking saveAndFlush(Booking booking) {
        return bookingRepository.saveAndFlush(booking); 
    }
    
}