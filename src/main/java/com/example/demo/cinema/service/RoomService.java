package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.Room;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;

    @Autowired
    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional(readOnly = true)
    public List<Room> getAllActiveRooms() {
        return roomRepository.findByIsActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Page<Room> findAllRooms(Pageable pageable) {
        return roomRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }

     @Transactional(readOnly = true)
     public Room getRoomById(Long id) {
         return roomRepository.findById(id)
                 .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
     }

    @Transactional
    public Room createRoom(Room room) {
        room.setActive(true);
        return roomRepository.save(room);
    }

    @Transactional
    public Room updateRoom(Long roomId, Room roomDetails) {
        Room existingRoom = getRoomById(roomId);
        
        existingRoom.setName(roomDetails.getName());
        existingRoom.setCapacity(roomDetails.getCapacity());
        existingRoom.setActive(roomDetails.isActive());
        
        return roomRepository.save(existingRoom);
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room not found with id: " + roomId + ". Cannot delete.");
        }
        roomRepository.deleteById(roomId);
    }
}