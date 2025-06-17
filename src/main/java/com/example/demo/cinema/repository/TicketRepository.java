package com.example.demo.cinema.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.cinema.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @Query("SELECT t FROM Ticket t WHERE t.booking.showtime.id = :showtimeId")
    List<Ticket> findByShowtimeId(@Param("showtimeId") Long showtimeId);

    @Query("SELECT t.seat.id FROM Ticket t WHERE t.booking.showtime.id = :showtimeId")
    Set<Long> findSeatIdsByShowtimeId(@Param("showtimeId") Long showtimeId);
}