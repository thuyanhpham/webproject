package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.Room;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.repository.RoomRepository;

import org.hibernate.query.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }

    public Room save(Room room) {
        return roomRepository.save(room);
    }

    public void deleteById(Long id) {
        roomRepository.deleteById(id);
    }
    public List<Room> getAllActiveRooms() {
        return roomRepository.findByIsActiveTrueOrderByNameAsc();
    }
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    }
    public Room createRoom(Room room) {
        room.setActive(true);
        return roomRepository.save(room);
    }
    public Room updateRoom(Long roomId, Room roomDetails) {
        Room existingRoom = getRoomById(roomId);
        
        existingRoom.setName(roomDetails.getName());
        existingRoom.setCapacity(roomDetails.getCapacity());
        existingRoom.setActive(roomDetails.isActive());
        
        return roomRepository.save(existingRoom);
    }
    public void deleteRoom(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room not found with id: " + roomId + ". Cannot delete.");
        }
        roomRepository.deleteById(roomId);
    }
}
