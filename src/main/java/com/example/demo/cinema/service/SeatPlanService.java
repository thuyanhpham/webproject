package com.example.demo.cinema.service;

import com.example.demo.cinema.dto.SeatDisplayDTO;
import com.example.demo.cinema.dto.SeatPlanResponseDTO;
import com.example.demo.cinema.dto.SeatStatus;
import com.example.demo.cinema.dto.SeatTypeDTO;
import com.example.demo.cinema.entity.*;
import com.example.demo.cinema.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatPlanService {

    @Autowired private ShowtimeRepository showtimeRepository;
    @Autowired private SeatRepository seatRepository;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private SeatTypeRepository seatTypeRepository;

    @Transactional(readOnly = true)
    public SeatPlanResponseDTO getSeatPlanForShowtime(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Showtime not found with id: " + showtimeId));

        List<Seat> allSeatsInRoom = seatRepository.findByRoomIdOrderByRowOrderAscSeatNumberAsc(showtime.getRoom().getId());

        Set<Long> bookedSeatIds = ticketRepository.findSeatIdsByShowtimeId(showtimeId);

        List<SeatDisplayDTO> seatDisplayDTOs = new ArrayList<>();

    for (Seat seat : allSeatsInRoom) {
        if (!seat.isActive()) continue;

        SeatType seatType = seat.getSeatType();
        boolean isBooked = !seat.isAvailable() || bookedSeatIds.contains(seat.getId());
        
        if (seatType.isCouple()) {
            SeatDisplayDTO leftSeat = new SeatDisplayDTO();
            leftSeat.setSeatId(seat.getId()); 
            leftSeat.setSeatLabel(seat.getRowIdentifier() + seat.getSeatNumber());
            leftSeat.setPrice(seatType.getPrice()); 
            leftSeat.setStatus(isBooked ? SeatStatus.BOOKED : SeatStatus.AVAILABLE);
            leftSeat.setTypeName(seatType.getName());
            leftSeat.setColor(seatType.getColor());
            leftSeat.setCouple(true);
            leftSeat.setCouplePart("left"); 
            
            SeatDisplayDTO rightSeat = new SeatDisplayDTO();
            rightSeat.setSeatId(seat.getId()); 
            rightSeat.setSeatLabel(seat.getRowIdentifier() + (seat.getSeatNumber() + 1)); 
            rightSeat.setPrice(seatType.getPrice());
            rightSeat.setStatus(isBooked ? SeatStatus.BOOKED : SeatStatus.AVAILABLE);
            rightSeat.setTypeName(seatType.getName());
            rightSeat.setColor(seatType.getColor());
            rightSeat.setCouple(true);
            rightSeat.setCouplePart("right");

            seatDisplayDTOs.add(leftSeat);
            seatDisplayDTOs.add(rightSeat);

        } else {
            SeatDisplayDTO singleSeat = new SeatDisplayDTO();
            singleSeat.setSeatId(seat.getId());
            singleSeat.setSeatLabel(seat.getRowIdentifier() + seat.getSeatNumber());
            singleSeat.setPrice(showtime.getPrice() != null ? showtime.getPrice() : seatType.getPrice());
            singleSeat.setStatus(isBooked ? SeatStatus.BOOKED : SeatStatus.AVAILABLE);
            singleSeat.setTypeName(seatType.getName());
            singleSeat.setColor(seatType.getColor());
            singleSeat.setCouple(false);
            singleSeat.setCouplePart(null);

            seatDisplayDTOs.add(singleSeat);
        }
    }

        Map<String, List<SeatDisplayDTO>> seatLayout = seatDisplayDTOs.stream()
            .collect(Collectors.groupingBy(
                dto -> dto.getSeatLabel().replaceAll("\\d", ""), 
                LinkedHashMap::new,
                Collectors.toList()
            ));

        List<SeatTypeDTO> legend = seatTypeRepository.findAll().stream()
                .map(st -> new SeatTypeDTO(showtimeId, st.getName(), st.getColor(), false, null))
                .collect(Collectors.toList());
        return new SeatPlanResponseDTO(showtime, seatLayout, legend);
    }
}
