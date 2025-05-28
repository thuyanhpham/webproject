package com.example.demo.cinema.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.cinema.entity.BookingStatus;
import com.example.demo.cinema.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
	Set<Ticket> findByShowtimeIdAndStatusNot(Long showtimeId, BookingStatus status);
	boolean existsByShowtimeIdAndSeatIdAndStatusNot(Long showtimeId, Long seatId, BookingStatus status);
	
	@Query("SELECT t.seat.id FROM Ticket t WHERE t.showtime.id = :showtimeId AND t.status != 'CANCELLED'")
	Set<Long> findBookedSeatIdsByShowtimeId(@Param("showtimeId") Long showtimeId);

}
