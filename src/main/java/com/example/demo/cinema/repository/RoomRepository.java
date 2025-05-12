package com.example.demo.cinema.repository;

import com.example.demo.cinema.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // Tìm tất cả các phòng đang hoạt động (isActive = true)
    List<Room> findByIsActiveTrueOrderByNameAsc();
    Optional<Room> findByName(String name);

    // (Tùy chọn) Tìm phòng theo tên (không phân biệt hoa thường)
    // Optional<Room> findByNameIgnoreCase(String name);

    // (Tùy chọn) Tìm phòng theo tên và cinemaId (nếu có cinema)
    // List<Room> findByCinemaIdAndIsActiveTrueOrderByNameAsc(Long cinemaId);
}