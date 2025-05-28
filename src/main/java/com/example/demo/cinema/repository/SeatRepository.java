package com.example.demo.cinema.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.cinema.entity.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {

	List<Seat> findByRoomIdOrderByRowIdentifierAscSeatNumberAsc(Long roomId);
	List<Seat> findByRoomId(Long roomId);
	List<Seat> findByIdIn(List<Long> selectedSeatIds);
}
