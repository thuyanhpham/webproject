package com.example.demo.cinema.service; // Hoặc package của bạn

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

    // --- Lấy tất cả các phòng đang hoạt động (cho dropdown trong form Showtime) ---
    @Transactional(readOnly = true)
    public List<Room> getAllActiveRooms() {
        log.debug("Fetching all active rooms");
        return roomRepository.findByIsActiveTrueOrderByNameAsc();
    }

    // --- Lấy tất cả các phòng (có phân trang, cho trang quản lý Room nếu có) ---
    @Transactional(readOnly = true)
    public Page<Room> findAllRooms(Pageable pageable) {
        log.debug("Fetching all rooms with pagination");
        return roomRepository.findAll(pageable);
    }

    // --- Tìm phòng theo ID ---
    @Transactional(readOnly = true)
    public Optional<Room> findById(Long id) {
        log.debug("Finding room by ID: {}", id);
        return roomRepository.findById(id);
    }

     @Transactional(readOnly = true)
     public Room getRoomById(Long id) {
         log.debug("Getting room by ID: {}", id);
         return roomRepository.findById(id)
                 .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
     }


    // --- Tạo mới phòng ---
    @Transactional
    public Room createRoom(Room room) {
        log.info("Creating new room: {}", room.getName());
        // Có thể thêm logic kiểm tra trùng tên phòng trong cùng một cinema nếu có
        room.setActive(true); // Mặc định khi tạo là active
        return roomRepository.save(room);
    }

    // --- Cập nhật phòng ---
    @Transactional
    public Room updateRoom(Long roomId, Room roomDetails) {
        log.info("Updating room with ID: {}", roomId);
        Room existingRoom = getRoomById(roomId);

        existingRoom.setName(roomDetails.getName());
        existingRoom.setCapacity(roomDetails.getCapacity());
        existingRoom.setActive(roomDetails.isActive());
        // Cập nhật cinema nếu có

        return roomRepository.save(existingRoom);
    }

    // --- Xóa phòng (Cẩn thận với các suất chiếu đang sử dụng phòng này) ---
    @Transactional
    public void deleteRoom(Long roomId) {
        log.warn("Attempting to delete room with ID: {}", roomId);
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room not found with id: " + roomId + ". Cannot delete.");
        }
        // !!! CẢNH BÁO: Cần xử lý logic nếu phòng này đang được sử dụng bởi các suất chiếu
        // Có thể không cho xóa, hoặc chuyển các suất chiếu đó sang phòng khác, hoặc đánh dấu phòng là inactive
        // Ví dụ đơn giản là chỉ xóa:
        roomRepository.deleteById(roomId);
        log.info("Room deleted successfully with ID: {}", roomId);
    }
}