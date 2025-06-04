package com.example.demo.cinema.service;

import com.example.demo.cinema.dto.RoomFormDTO;
import com.example.demo.cinema.dto.RowDefinitionDTO;
import com.example.demo.cinema.entity.Room;
import com.example.demo.cinema.entity.Seat;
import com.example.demo.cinema.entity.SeatType;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.repository.RoomRepository;
import com.example.demo.cinema.repository.SeatTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final SeatService seatService;
    private final SeatTypeRepository seatTypeRepository;

    private static final String DEFAULT_SEAT_TYPE_NAME = "Normal"; // Đảm bảo tên này có trong bảng seat_types

    @Autowired
    public RoomService(RoomRepository roomRepository, SeatService seatService, SeatTypeRepository seatTypeRepository) {
        this.roomRepository = roomRepository;
        this.seatService = seatService;
        this.seatTypeRepository = seatTypeRepository;
    }

    public List<Room> getAllActiveRooms() {
        return roomRepository.findByIsActiveTrueOrderByNameAsc();
    }

    public List<Room> getAllRoomsOrderedByName() {
        log.debug("Fetching all rooms, ordered by name.");
        return roomRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }
    
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    }
    
    public Optional<Room> findRoomOptionalById(Long id) {
        log.debug("Finding room by ID (optional): {}", id);
        return roomRepository.findById(id);
    }

    public Room findByIdOrThrow(Long id) {
        log.debug("Finding room by ID or throwing exception: {}", id);
        return roomRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Room not found with ID: {}", id);
                    return new ResourceNotFoundException("Room not found with id: " + id);
                });
    }
    
    public RoomFormDTO getRoomFormDTOForEdit(Long roomId) {
        log.info("Preparing RoomFormDTO for editing room ID: {}", roomId);
        Room roomEntity = findByIdOrThrow(roomId);
        return convertEntityToFormDTO(roomEntity);
    }

    private RoomFormDTO convertEntityToFormDTO(Room roomEntity) {
        // ... (Giữ nguyên logic convertEntityToFormDTO đã đúng) ...
        log.debug("Converting Room entity (ID: {}) to RoomFormDTO.", roomEntity.getId());
        RoomFormDTO roomForm = new RoomFormDTO();
        roomForm.setId(roomEntity.getId());
        roomForm.setName(roomEntity.getName());

        List<Seat> seatsInRoom = seatService.findByRoomIdOrderByRowIdentifierAscSeatNumberAsc(roomEntity.getId());
        log.debug("Found {} seats for room ID: {}.", seatsInRoom.size(), roomEntity.getId());

        if (!seatsInRoom.isEmpty()) {
            Map<String, List<Seat>> seatsByRowMap = seatsInRoom.stream()
                .collect(Collectors.groupingBy(
                    Seat::getRowIdentifier,
                    LinkedHashMap::new,
                    Collectors.toList()
                ));

            List<RowDefinitionDTO> rowDefinitions = new ArrayList<>();
            List<String> sortedRowIdentifiers = new ArrayList<>(seatsByRowMap.keySet());
            sortedRowIdentifiers.sort(yourCustomRowIdentifierComparator());
            log.debug("Sorted row identifiers for DTO conversion: {}", sortedRowIdentifiers);

            int currentOrderForDTO = 1;
            for (String rowId : sortedRowIdentifiers) {
                List<Seat> seatsInThisRow = seatsByRowMap.get(rowId);
                if (seatsInThisRow != null && !seatsInThisRow.isEmpty()) {
                    RowDefinitionDTO def = new RowDefinitionDTO();
                    def.setIdentifier(rowId);
                    def.setNumberOfSeats(seatsInThisRow.size());
                    def.setOrder(currentOrderForDTO++);

                    Seat firstSeatInRow = seatsInThisRow.get(0);
                    if (firstSeatInRow.getSeatType() != null) {
                        def.setSeatTypeId(firstSeatInRow.getSeatType().getId());
                    }
                    rowDefinitions.add(def);
                }
            }
            roomForm.setRowDefinitions(rowDefinitions);
            log.debug("Converted to {} RowDefinitionDTOs.", rowDefinitions.size());
        }
        return roomForm;
    }


    @Transactional
    public Room saveRoomAndLayout(RoomFormDTO roomFormDTO) {
        log.info("Service: Processing save/update for room DTO: name='{}', id={}", roomFormDTO.getName(), roomFormDTO.getId());

        Room roomEntity;
        boolean isNewRoom = roomFormDTO.getId() == null;

        if (isNewRoom) {
            log.info("Service: Creating a new room.");
            roomEntity = new Room();
            roomEntity.setActive(true);
        } else {
            log.info("Service: Updating existing room with ID: {}.", roomFormDTO.getId());
            roomEntity = findByIdOrThrow(roomFormDTO.getId());
            log.info("Service: Deleting existing seats for room ID: {} before update.", roomEntity.getId());
            seatService.deleteByRoomId(roomEntity.getId());
            log.info("Service: Existing seats deleted for room ID: {}", roomEntity.getId());
        }

        roomEntity.setName(roomFormDTO.getName());
        roomRepository.save(roomEntity);
        log.info("Service: Room entity basic info {} (ID: {}).", (isNewRoom ? "created" : "updated"), roomEntity.getId());

        List<Seat> seatsToCreate = new ArrayList<>();
        int totalCapacity = 0;
        List<RowDefinitionDTO> rowDefinitions = roomFormDTO.getRowDefinitions();

        // ===================== SỬA LỖI Ở ĐÂY =========================
        final SeatType defaultSeatTypeObject; // Đổi tên biến và kiểu cho rõ ràng
        Optional<SeatType> defaultSeatTypeOpt = seatTypeRepository.findByName(DEFAULT_SEAT_TYPE_NAME); // seatTypeRepository phải trả về Optional<SeatType>

        if (defaultSeatTypeOpt.isPresent()) {
            defaultSeatTypeObject = defaultSeatTypeOpt.get(); // Gán đối tượng SeatType
            log.debug("Service: Using default SeatType: '{}' (ID: {})", defaultSeatTypeObject.getName(), defaultSeatTypeObject.getId());
        } else {
            String errorMsg = "FATAL: Default SeatType named '" + DEFAULT_SEAT_TYPE_NAME + "' not found. Please ensure it exists in the database.";
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        // ============================================================

        if (rowDefinitions != null && !rowDefinitions.isEmpty()) {
            rowDefinitions.sort(Comparator.comparing(RowDefinitionDTO::getOrder, Comparator.nullsLast(Integer::compareTo)));
            log.debug("Service: Processing {} row definitions from DTO, sorted by order.", rowDefinitions.size());

            for (RowDefinitionDTO def : rowDefinitions) {
                if (isValidRowDefinition(def)) {
                    String rowId = def.getIdentifier().trim().toUpperCase();
                    // ===================== SỬA LỖI Ở ĐÂY =========================
                    SeatType seatTypeForRow = defaultSeatTypeObject; // Khởi tạo bằng đối tượng SeatType mặc định
                    // ============================================================

                    if (def.getSeatTypeId() != null) {
                        Optional<SeatType> foundSeatType = seatTypeRepository.findById(def.getSeatTypeId()); // Đúng
                        if (foundSeatType.isPresent()) {
                            seatTypeForRow = foundSeatType.get(); // Gán SeatType tìm thấy
                        } else {
                            log.warn("SeatType with ID {} for row '{}' not found. Using default type '{}'.",
                                     def.getSeatTypeId(), def.getIdentifier(), defaultSeatTypeObject.getName()); // Log tên của defaultSeatTypeObject
                        }
                    }
                    // log.trace nên dùng tên của seatTypeForRow để đảm bảo
                    log.trace("Service: For row '{}', using SeatType: '{}' (ID: {})", rowId, seatTypeForRow.getName(), seatTypeForRow.getId());

                    for (int i = 1; i <= def.getNumberOfSeats(); i++) {
                        Seat seat = new Seat();
                        seat.setRoom(roomEntity);
                        seat.setRowIdentifier(rowId);
                        seat.setSeatNumber(i);
                        seat.setSeatLabel(rowId + i);
                        seat.setAvailable(true);
                        // ===================== SỬA LỖI Ở ĐÂY =========================
                        seat.setSeatType(seatTypeForRow); // Gán đối tượng SeatType cho Seat
                        // ============================================================
                        seat.setActive(true);
                        seatsToCreate.add(seat);
                        totalCapacity++;
                    }
                } else {
                    log.warn("Service: Invalid row definition skipped: Identifier='{}', Seats={}, Order={}",
                             def.getIdentifier(), def.getNumberOfSeats(), def.getOrder());
                }
            }
        }

        if (!seatsToCreate.isEmpty()) {
            seatService.saveAll(seatsToCreate);
            log.info("Service: Created {} new seats for room ID: {}", seatsToCreate.size(), roomEntity.getId());
        } else {
            log.info("Service: No seats to create for room ID: {}", roomEntity.getId() != null ? roomEntity.getId() : "NEW (no rows defined)");
        }

        roomEntity.setCapacity(totalCapacity);
        Room finalSavedRoom = roomRepository.save(roomEntity);
        log.info("Service: Room '{}' (ID: {}) finalized save/update with capacity {}. Layout contains {} seats.",
                 finalSavedRoom.getName(), finalSavedRoom.getId(), finalSavedRoom.getCapacity(), seatsToCreate.size());
        return finalSavedRoom;
    }
    
    @Transactional
    public void deleteRoomById(Long id) {
        if (!roomRepository.existsById(id)) {
            log.warn("Attempted to delete non-existent room with ID: {}", id);
            throw new ResourceNotFoundException("Room not found with id: " + id + " for deletion.");
        }
        log.info("Service: Attempting to delete seats for room ID: {}", id);
        seatService.deleteByRoomId(id);
        log.info("Service: Deleting room with ID: {}", id);
        roomRepository.deleteById(id);
        log.info("Service: Successfully deleted room with ID: {}", id);
    }

    private boolean isValidRowDefinition(RowDefinitionDTO def) {
        // ... (giữ nguyên)
        return def != null &&
               def.getIdentifier() != null && !def.getIdentifier().trim().isEmpty() &&
               def.getNumberOfSeats() != null && def.getNumberOfSeats() > 0 &&
               def.getOrder() != null && def.getOrder() >= 0;
    }

    private Comparator<String> yourCustomRowIdentifierComparator() {
        // ... (giữ nguyên)
        return (s1, s2) -> {
            if (s1 == null && s2 == null) return 0;
            if (s1 == null) return -1;
            if (s2 == null) return 1;
            if (s1.length() != s2.length()) {
                return Integer.compare(s1.length(), s2.length());
            }
            return s1.compareTo(s2);
        };
    }

}