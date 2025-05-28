package com.example.demo.cinema.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.demo.cinema.entity.Booking;
import com.example.demo.cinema.entity.BookingStatus;
import com.example.demo.cinema.entity.Seat;
import com.example.demo.cinema.entity.SeatType;
import com.example.demo.cinema.entity.Showtime;
import com.example.demo.cinema.entity.Ticket;
import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.repository.SeatRepository;
import com.example.demo.cinema.repository.ShowtimeRepository;
import com.example.demo.cinema.repository.TicketRepository;
import com.example.demo.cinema.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class BookingService {

    private final TicketRepository ticketRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    public BookingService(TicketRepository ticketRepository,
                          ShowtimeRepository showtimeRepository,
                          SeatRepository seatRepository,
                          UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Booking createBooking(Long showtimeId, List<Long> selectedSeatIds, String username) {

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Seat> seatsToBook = seatRepository.findByIdIn(selectedSeatIds);
        if (seatsToBook.size() != selectedSeatIds.size()) {
            throw new IllegalArgumentException("One or more selected seats are invalid.");
        }

        // Kiểm tra ghế đã được đặt
        Set<Long> alreadyBookedSeatIds = ticketRepository.findBookedSeatIdsByShowtimeId(showtimeId);
        for (Seat seat : seatsToBook) {
            if (!seat.isActive()) {
                throw new IllegalArgumentException("Seat " + seat.getRowIdentifier() + seat.getSeatNumber() + " is not available.");
            }
            if (alreadyBookedSeatIds.contains(seat.getId())) {
                throw new IllegalArgumentException("Seat " + seat.getRowIdentifier() + seat.getSeatNumber() + " is already booked.");
            }
        }

        // Tạo thời gian đặt
        LocalDateTime bookingTime = LocalDateTime.now();

        // Tạo booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setBookingTime(bookingTime);
        booking.setStatus(BookingStatus.CONFIRMED);

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<Ticket> tickets = new ArrayList<>();

        for (Seat seat : seatsToBook) {
            BigDecimal seatPrice = calculateSeatPrice(showtime.getPrice(), seat.getSeatType());

            Ticket ticket = new Ticket();
            ticket.setShowtime(showtime);
            ticket.setSeat(seat);
            ticket.setUser(user);
            ticket.setBookingTime(bookingTime);
            ticket.setStatus(BookingStatus.CONFIRMED);
            ticket.setBooking(booking);
            ticket.setPrice(seatPrice);

            tickets.add(ticket);
            totalPrice = totalPrice.add(seatPrice);
        }

        booking.setTotalPrice(totalPrice);

        // Lưu tất cả vé (Booking được lưu cascade nếu mapping đúng)
        ticketRepository.saveAll(tickets);

        return booking;
    }

    // Hàm tính giá vé theo loại ghế
    private BigDecimal calculateSeatPrice(BigDecimal basePrice, SeatType seatType) {
        if (basePrice == null) basePrice = BigDecimal.ZERO;

        switch (seatType) {
            case VIP:
                return basePrice.multiply(BigDecimal.valueOf(1.5));
            case COUPLE:
                return basePrice.multiply(BigDecimal.valueOf(2.0));
            default:
                return basePrice;
        }
    }
}
