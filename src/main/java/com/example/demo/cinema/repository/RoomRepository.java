package com.example.demo.cinema.repository;

import com.example.demo.cinema.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByIsActiveTrueOrderByNameAsc();
    Optional<Room> findByName(String name);

}