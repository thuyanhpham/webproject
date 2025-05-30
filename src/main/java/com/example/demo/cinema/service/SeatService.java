package com.example.demo.cinema.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.cinema.entity.Seat;
import com.example.demo.cinema.repository.SeatRepository;

@Service
public class SeatService {

	@Autowired
	private SeatRepository seatRepository;
	
	public Seat save(Seat seat) {
		return seatRepository.save(seat);
	}
	public List<Seat> saveAll(List<Seat> seats) {
		return seatRepository.saveAll(seats);
	}
	public Optional<Seat> findById(Long id) {
		return seatRepository.findById(id);
	}
	public List<Seat> findAll() {
		return seatRepository.findAll();
	}
	public void deleteById(Long id) {
		seatRepository.deleteById(id);
	}
}
