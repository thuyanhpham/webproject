package com.example.demo.cinema.service;

import com.example.demo.cinema.dto.SeatDTO;
import com.example.demo.cinema.dto.SeatIdentifierDTO;
import com.example.demo.cinema.dto.SeatMapDTO;
import com.example.demo.cinema.dto.UpdateSeatStatusByAdminRequestDTO;
import com.example.demo.cinema.entity.*;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.repository.SeatRepository;
import com.example.demo.cinema.repository.ShowtimeRepository;
import com.example.demo.cinema.repository.TicketRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatManagementService {

    private static final Logger log = LoggerFactory.getLogger(SeatManagementService.class);

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;

    @Autowired
    public SeatManagementService(ShowtimeRepository showtimeRepository,
                                 SeatRepository seatRepository,
                                 TicketRepository ticketRepository) {
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public SeatMapDTO getSeatMapForShowtime(Long showtimeId) {
        Showtime showtime = showtimeRepository.findByIdWithMovieAndRoomAndSeats(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + showtimeId));

        Room room = showtime.getRoom();
        Movie movie = showtime.getMovie();

        List<Seat> allSeatsInRoom = room.getSeats().stream()
                .sorted(Comparator.comparing(Seat::getRowIdentifier).thenComparing(Seat::getSeatNumber))
                .collect(Collectors.toList());

        // ✅ Gán kết quả vào bookedSeatIds
        Set<Long> bookedSeatIds = ticketRepository.findByShowtimeIdAndStatusNot(showtimeId, BookingStatus.CANCELLED)
                .stream()
                .map(ticket -> ticket.getSeat().getId())
                .collect(Collectors.toSet());

        List<SeatDTO> seatDTOs = new ArrayList<>();
        Map<String, List<SeatDTO>> groupedSeats = new LinkedHashMap<>();

        for (Seat seat : allSeatsInRoom) {
            String status = "AVAILABLE";
            if (!seat.isActive()) {
                status = "UNAVAILABLE_SYSTEM";
            } else if (bookedSeatIds.contains(seat.getId())) {
                status = "BOOKED";
            }

            BigDecimal seatPrice = showtime.getPrice() != null ? showtime.getPrice() : BigDecimal.ZERO;

            if (seat.getSeatType() == SeatType.VIP) {
                seatPrice = seatPrice.multiply(BigDecimal.valueOf(1.5));
            } else if (seat.getSeatType() == SeatType.COUPLE) {
                seatPrice = seatPrice.multiply(BigDecimal.valueOf(2.0));
            }

            SeatDTO dto = new SeatDTO();
            dto.setSeatId(seat.getId());
            dto.setRowIdentifier(seat.getRowIdentifier());
            dto.setSeatNumber(seat.getSeatNumber());
            dto.setSeatType(seat.getSeatType() != null ? seat.getSeatType().name() : "NORMAL");
            dto.setStatus(status);
            dto.setPhysicallyAvailable(seat.isActive());
            dto.setPrice(seatPrice);

            seatDTOs.add(dto);
            groupedSeats.computeIfAbsent(seat.getRowIdentifier(), k -> new ArrayList<>()).add(dto);
        }

        // Tạo SeatMapDTO
        SeatMapDTO seatMapDTO = new SeatMapDTO();
        seatMapDTO.setShowtimeId(showtime.getId());
        seatMapDTO.setMovieId(movie.getId());
        seatMapDTO.setMovieTitle(movie.getTitle());
        seatMapDTO.setMovieBannerUrl(movie.getBannerUrl());

        DateTimeFormatter dateFormatterUrl = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateDisplayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter timeDisplayFormatter = DateTimeFormatter.ofPattern("HH:mm");

        seatMapDTO.setShowDateForUrl(showtime.getShowDate().format(dateFormatterUrl));
        seatMapDTO.setShowFullDateDisplay(showtime.getShowDate().format(dateDisplayFormatter));
        seatMapDTO.setShowStartTimeDisplay(showtime.getStartTime().format(timeDisplayFormatter));

        seatMapDTO.setRoomName(room.getName());
        seatMapDTO.setRoomCapacity(room.getCapacity());
        seatMapDTO.setDefaultShowtimePrice(showtime.getPrice());
        seatMapDTO.setSeats(seatDTOs);
        seatMapDTO.setGroupedSeatsByRow(groupedSeats);

        return seatMapDTO;
    }


    @Transactional
    public void updateSeatStatusesByAdmin(Long showtimeId, UpdateSeatStatusByAdminRequestDTO requestDTO, String adminUsername) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found: " + showtimeId));

        List<Long> seatIdsToUpdate = requestDTO.getSeatsToUpdate().stream()
                .map(SeatIdentifierDTO::getSeatId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (seatIdsToUpdate.isEmpty()) {
            throw new IllegalArgumentException("No seat IDs provided for update.");
        }

        List<Seat> seats = seatRepository.findAllById(seatIdsToUpdate);
        if (seats.size() != seatIdsToUpdate.size()) {
            throw new ResourceNotFoundException("One or more seats not found for update.");
        }

        String newAdminStatus = requestDTO.getNewAdminStatus();

        for (Seat seat : seats) {
            if ("UNAVAILABLE_BY_ADMIN".equalsIgnoreCase(newAdminStatus)) {
                seat.setActive(false);
            } else if ("AVAILABLE".equalsIgnoreCase(newAdminStatus)) {
                seat.setActive(true);
            }
        }

        seatRepository.saveAll(seats);
        log.info("Admin '{}' updated seat status to '{}' for showtime {}", adminUsername, newAdminStatus, showtimeId);
    }
}
