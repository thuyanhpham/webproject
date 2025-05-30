package com.example.demo.cinema.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.cinema.entity.SeatType;

public interface SeatTypeRepository extends JpaRepository<SeatType, Long> {

	Optional<SeatType> findByNameIgnoreCase(String name);
}
