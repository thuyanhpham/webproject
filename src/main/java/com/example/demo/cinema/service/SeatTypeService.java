package com.example.demo.cinema.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.cinema.entity.SeatType;
import com.example.demo.cinema.repository.SeatTypeRepository;

import jakarta.transaction.Transactional;

@Service 
public class SeatTypeService {

	@Autowired
	private SeatTypeRepository seatTypeRepository;
	
	public List<SeatType> findAll() {
		return seatTypeRepository.findAll();
	}
	
	public Optional<SeatType> findById(Long id) {
		return seatTypeRepository.findById(id);
	}
	
	public SeatType save(SeatType seatType) {
		return seatTypeRepository.save(seatType);
	}
	
	public void deleteById(Long id) {
		seatTypeRepository.deleteById(id);
	}
	
	public Optional<SeatType> findByNameIgnoreCase(String name) {
		return seatTypeRepository.findByNameIgnoreCase(name);
	}
	
	@Transactional
	public List<SeatType> findAllActive() {
		List<SeatType> types = seatTypeRepository.findByIsActiveTrueOrderByNameAsc();
		return types;
	}
}
