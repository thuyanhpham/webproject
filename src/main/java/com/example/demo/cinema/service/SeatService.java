package com.example.demo.cinema.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort; 
import org.springframework.stereotype.Service;

import com.example.demo.cinema.entity.Room;
import com.example.demo.cinema.entity.Seat;
import com.example.demo.cinema.entity.SeatType;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.repository.SeatRepository;
import com.example.demo.cinema.repository.SeatTypeRepository;

@Service
public class SeatService {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SeatTypeRepository seatTypeRepository;

    public Seat save(Seat seat) {
        if (seat.getSeatLabel() == null || seat.getSeatLabel().isBlank()) {
            seat.setSeatLabel(seat.getRowIdentifier() + seat.getSeatNumber());
        }
        return seatRepository.save(seat);
    }

    public List<Seat> saveAll(List<Seat> seats) {
        seats.forEach(seat -> {
            if (seat.getSeatLabel() == null || seat.getSeatLabel().isBlank()) {
                seat.setSeatLabel(seat.getRowIdentifier() + seat.getSeatNumber());
            }
        });
        return seatRepository.saveAll(seats);
    }

    public Optional<Seat> findById(Long id) {
        return seatRepository.findById(id);
    }

    public List<Seat> findByRoomOrderByRowIdentifierAscSeatNumberAsc(Room room) {
        return seatRepository.findByRoomOrderByRowIdentifierAscSeatNumberAsc(room);
    }

    public List<Seat> findByRoomIdOrderByRowIdentifierAscSeatNumberAsc(Long roomId) {
        return seatRepository.findByRoomIdOrderByRowIdentifierAscSeatNumberAsc(roomId);
    }

    public void deleteById(Long id) {
        seatRepository.deleteById(id);
    }

    public void deleteByRoomId(Long roomId) {
        try {
            seatRepository.deleteByRoomId(roomId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete seats for room " + roomId, e);
        }
    }

    public Seat updateSeatType(Long seatId, Long seatTypeId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + seatId));
        
        SeatType newSeatType = seatTypeRepository.findById(seatTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("SeatType not found with id: " + seatTypeId));
        
        seat.setSeatType(newSeatType);
        return seatRepository.save(seat);
    }

    public List<Seat> findByRoomIdOrderByRowOrderAscSeatNumberAsc(Long roomId) { 
        return seatRepository.findByRoomId(roomId, Sort.by(Sort.Order.asc("rowOrder"), Sort.Order.asc("seatNumber")));
    }
}

