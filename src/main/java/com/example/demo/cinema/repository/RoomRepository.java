package com.example.demo.cinema.repository;

import com.example.demo.cinema.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByIsActiveTrueOrderByNameAsc();
    Optional<Room> findByName(String name);
    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.seats WHERE r.id = :roomId")
    Optional<Room> findByIdWithSeats(@Param("roomId") Long roomId);
}