package com.example.demo.cinema.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.cinema.entity.Room;
import com.example.demo.cinema.entity.Seat;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

	List<Seat> findByRoomOrderByRowIdentifierAscSeatNumberAsc(Room room);
    List<Seat> findByRoomIdOrderByRowIdentifierAscSeatNumberAsc(Long roomId);

    void deleteByRoomId(Long roomId);
}
