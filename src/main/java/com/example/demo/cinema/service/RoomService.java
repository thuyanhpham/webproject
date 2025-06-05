package com.example.demo.cinema.service;

import com.example.demo.cinema.dto.RoomFormDTO;
import com.example.demo.cinema.dto.RowDefinitionDTO;
import com.example.demo.cinema.dto.SeatAssignmentDTO;
import com.example.demo.cinema.dto.SeatInAssignmentDTO;
import com.example.demo.cinema.entity.Room;
import com.example.demo.cinema.entity.Seat;
import com.example.demo.cinema.entity.SeatType;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.repository.RoomRepository;
import com.example.demo.cinema.repository.SeatTypeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final SeatService seatService;
    private final SeatTypeRepository seatTypeRepository;
    private final ObjectMapper objectMapper;

    private static final String DEFAULT_SEAT_TYPE_NAME_FOR_NEW_SEATS = "Normal";

    @Autowired
    public RoomService(RoomRepository roomRepository, SeatService seatService, SeatTypeRepository seatTypeRepository, ObjectMapper objectMapper) {
        this.roomRepository = roomRepository;
        this.seatService = seatService;
        this.seatTypeRepository = seatTypeRepository;
        this.objectMapper = objectMapper;
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
        RoomFormDTO roomForm = new RoomFormDTO();
        roomForm.setId(roomEntity.getId());
        roomForm.setName(roomEntity.getName());
        roomForm.setActive(roomEntity.isActive());

        List<Seat> seatsInRoom = seatService.findByRoomIdOrderByRowOrderAscSeatNumberAsc(roomEntity.getId());
        log.debug("Found {} seats for room ID: {}.", seatsInRoom.size(), roomEntity.getId());

        // 1. Xây dựng List<RowDefinitionDTO> từ Seat entities
        List<RowDefinitionDTO> rowDefinitionDTOs = new ArrayList<>();
        if (!seatsInRoom.isEmpty()) {
            Map<String, List<Seat>> seatsByRowId = seatsInRoom.stream()
                .collect(Collectors.groupingBy(
                    Seat::getRowIdentifier,
                    LinkedHashMap::new, // Giữ thứ tự hàng dựa trên seat đầu tiên của mỗi hàng
                    Collectors.toList()
                ));

            for (Map.Entry<String, List<Seat>> entry : seatsByRowId.entrySet()) {
                String rowId = entry.getKey();
                List<Seat> seatsInThisRow = entry.getValue();

                if (!seatsInThisRow.isEmpty()) {
                    RowDefinitionDTO defDTO = new RowDefinitionDTO();
                    defDTO.setIdentifier(rowId);
                    defDTO.setNumberOfSeats(seatsInThisRow.size());
                    defDTO.setOrder(seatsInThisRow.get(0).getRowOrder() != null ? seatsInThisRow.get(0).getRowOrder() : 0);

                    // Xác định seatTypeId "chung" cho RowDefinitionDTO
                    Long commonSeatTypeId = null;
                    if (seatsInThisRow.get(0).getSeatType() != null) {
                        commonSeatTypeId = seatsInThisRow.get(0).getSeatType().getId();
                        for (int i = 1; i < seatsInThisRow.size(); i++) {
                            Seat currentSeat = seatsInThisRow.get(i);
                            if (currentSeat.getSeatType() == null || !commonSeatTypeId.equals(currentSeat.getSeatType().getId())) {
                                commonSeatTypeId = null; break;
                            }
                        }
                    }
                    defDTO.setSeatTypeId(commonSeatTypeId);
                    rowDefinitionDTOs.add(defDTO);
                }
            }
            // Sắp xếp DTOs lại nếu cần (mặc dù LinkedHashMap đã cố gắng giữ thứ tự)
            rowDefinitionDTOs.sort(Comparator.comparing(RowDefinitionDTO::getOrder, Comparator.nullsLast(Integer::compareTo))
                                           .thenComparing(RowDefinitionDTO::getIdentifier, yourCustomRowIdentifierComparator()));
        }
        roomForm.setRowDefinitions(rowDefinitionDTOs);
        log.debug("Built {} RowDefinitionDTOs for edit form.", rowDefinitionDTOs.size());

        // 2. Xây dựng seatAssignmentsJson (List<SeatAssignmentDTO> lồng nhau)
        List<SeatAssignmentDTO> seatAssignmentsListForJson = new ArrayList<>();
        if (!rowDefinitionDTOs.isEmpty()) { // Dựa trên DTOs đã tạo ở trên
             Map<String, List<Seat>> seatsByRowIdMapForJson = seatsInRoom.stream()
                .collect(Collectors.groupingBy(Seat::getRowIdentifier));

            for (RowDefinitionDTO defDto : rowDefinitionDTOs) {
                String rowId = defDto.getIdentifier();
                SeatAssignmentDTO rowAssignment = new SeatAssignmentDTO();
                rowAssignment.setRowIdentifier(rowId);
                List<Seat> seatsInThisRow = seatsByRowIdMapForJson.getOrDefault(rowId, Collections.emptyList());
                List<SeatInAssignmentDTO> seatDtosInRow = seatsInThisRow.stream()
                    .sorted(Comparator.comparingInt(Seat::getSeatNumber))
                    .map(seat -> {
                        SeatInAssignmentDTO seatDto = new SeatInAssignmentDTO();
                        seatDto.setSeatNumber(seat.getSeatNumber());
                        if (seat.getSeatType() != null) seatDto.setSeatTypeId(seat.getSeatType().getId());
                        else seatDto.setSeatTypeId(null);
                        return seatDto;
                    })
                    .collect(Collectors.toList());
                rowAssignment.setSeats(seatDtosInRow);
                seatAssignmentsListForJson.add(rowAssignment);
            }
        }
        try {
            roomForm.setSeatAssignmentsJson(objectMapper.writeValueAsString(seatAssignmentsListForJson));
            log.debug("Generated seatAssignmentsJson for edit form ({} SeatAssignmentDTOs): {}", seatAssignmentsListForJson.size(), roomForm.getSeatAssignmentsJson().substring(0, Math.min(200, roomForm.getSeatAssignmentsJson().length())));
        } catch (JsonProcessingException e) {
            log.error("Error converting seat assignments to JSON for room ID {}: {}", roomId, e.getMessage());
            roomForm.setSeatAssignmentsJson("[]");
        }
        return roomForm;
    }

@Transactional
    public Room saveRoomAndLayout(RoomFormDTO roomFormDTO) {
        log.info("Service: Processing save/update for room DTO: name='{}', id={}. Active: {}", roomFormDTO.getName(), roomFormDTO.getId(), roomFormDTO.isActive());

        Room roomEntity;
        boolean isNewRoom = roomFormDTO.getId() == null;

        if (isNewRoom) {
            log.info("Service: Creating a new room.");
            roomEntity = new Room();
        } else {
            log.info("Service: Updating existing room with ID: {}.", roomFormDTO.getId());
            roomEntity = findByIdOrThrow(roomFormDTO.getId());
            log.info("Service: Deleting existing seats for room ID: {} before update.", roomEntity.getId());
            seatService.deleteByRoomId(roomEntity.getId()); // Xóa tất cả Seat entities cũ
            // Không có RowDefinition entity để xóa khỏi Room
            log.info("Service: Existing seats cleared for room ID: {}", roomEntity.getId());
        }

        roomEntity.setName(roomFormDTO.getName());
        roomEntity.setActive(roomFormDTO.isActive());
        // roomEntity.setRowDefinitions(null); // Không có trường này trong Room entity nữa

        // Parse seatAssignmentsJson (dạng phẳng từ client) và chuyển thành List<SeatAssignmentDTO> (lồng nhau)
        List<SeatAssignmentDTO> seatAssignmentsNested = parseAndGroupFlatSeatAssignments(roomFormDTO.getSeatAssignmentsJson());
        Map<String, Map<Integer, Long>> assignedSeatTypesFromClientMap = new HashMap<>();
        if (seatAssignmentsNested != null && !seatAssignmentsNested.isEmpty()) {
            for (SeatAssignmentDTO rowAssignment : seatAssignmentsNested) {
                Map<Integer, Long> seatsInRowMap = new HashMap<>();
                if (rowAssignment.getSeats() != null) {
                    for (SeatInAssignmentDTO seatInAssignment : rowAssignment.getSeats()) {
                        seatsInRowMap.put(seatInAssignment.getSeatNumber(), seatInAssignment.getSeatTypeId());
                    }
                }
                assignedSeatTypesFromClientMap.put(rowAssignment.getRowIdentifier().trim().toUpperCase(), seatsInRowMap);
            }
            log.debug("Parsed and grouped assignedSeatTypesFromClientMap: {}", assignedSeatTypesFromClientMap);
        } else {
            log.warn("No seat assignments provided or parsed from JSON. Seats will use default or row-defined types.");
        }

        List<Seat> seatsToCreate = new ArrayList<>();
        int totalCapacity = 0;
        List<RowDefinitionDTO> rowDefinitionsFromClientDTO = roomFormDTO.getRowDefinitions();

        SeatType systemDefaultSeatType = seatTypeRepository.findByName(DEFAULT_SEAT_TYPE_NAME_FOR_NEW_SEATS)
            .orElseGet(() -> seatTypeRepository.findFirstByIsActiveTrue().orElseThrow(() ->
                new IllegalStateException("FATAL: No active SeatTypes found and default '" + DEFAULT_SEAT_TYPE_NAME_FOR_NEW_SEATS + "' is missing.")
            ));
        log.debug("Service: Using system default SeatType: '{}' (ID: {})", systemDefaultSeatType.getName(), systemDefaultSeatType.getId());

        if (rowDefinitionsFromClientDTO != null && !rowDefinitionsFromClientDTO.isEmpty()) {
            rowDefinitionsFromClientDTO.sort(Comparator.comparing(RowDefinitionDTO::getOrder, Comparator.nullsLast(Integer::compareTo))
                                                    .thenComparing(RowDefinitionDTO::getIdentifier, yourCustomRowIdentifierComparator()));
            log.debug("Service: Processing {} row definitions from DTO, sorted by order.", rowDefinitionsFromClientDTO.size());
                 for (RowDefinitionDTO defDTO : rowDefinitionsFromClientDTO) {
                if (isValidRowDefinition(defDTO)) {
                    String rowId = defDTO.getIdentifier().trim().toUpperCase();
                    Integer rowOrder = defDTO.getOrder();

                    final SeatType effectivelyFinalRowDefaultType;
                    if (defDTO.getSeatTypeId() != null) {
                        SeatType foundType = seatTypeRepository.findById(defDTO.getSeatTypeId()).orElse(null);
                        if (foundType == null) {
                             log.warn("SeatTypeId {} defined in RowDefinitionDTO for row {} not found. Defaulting to null for this row's default.", defDTO.getSeatTypeId(), rowId);
                             effectivelyFinalRowDefaultType = null;
                        } else {
                            effectivelyFinalRowDefaultType = foundType;
                        }
                    } else {
                        effectivelyFinalRowDefaultType = null;
                    }

                    Map<Integer, Long> specificAssignmentsForThisRow = assignedSeatTypesFromClientMap.getOrDefault(rowId, Collections.emptyMap());

                    for (int i = 1; i <= defDTO.getNumberOfSeats(); i++) {
                        // === TẠO BẢN SAO FINAL CHO i ===
                        final int currentSeatNumberInLoop = i; // Biến này là effectively final cho lambda

                        Seat seat = new Seat();
                        seat.setRoom(roomEntity);
                        seat.setRowIdentifier(rowId);
                        seat.setSeatNumber(currentSeatNumberInLoop); // Sử dụng bản sao
                        seat.setRowOrder(rowOrder);
                        seat.setSeatLabel(rowId + currentSeatNumberInLoop); // Sử dụng bản sao
                        seat.setAvailable(true);
                        seat.setActive(true);

                        Long specificSeatTypeId = specificAssignmentsForThisRow.get(currentSeatNumberInLoop); // Sử dụng bản sao
                        SeatType finalSeatType;

                        if (specificSeatTypeId != null) {
                            final Long capturedSeatTypeId = specificSeatTypeId;
                            finalSeatType = seatTypeRepository.findById(capturedSeatTypeId)
                                .orElseGet(() -> { // Lambda expression
                                    log.warn("SeatType with ID {} (from specific assignment for {}_{}) not found. Falling back.",
                                             capturedSeatTypeId, rowId, currentSeatNumberInLoop); // SỬ DỤNG currentSeatNumberInLoop
                                    return effectivelyFinalRowDefaultType != null ? effectivelyFinalRowDefaultType : systemDefaultSeatType;
                                });
                        } else if (effectivelyFinalRowDefaultType != null) {
                            finalSeatType = effectivelyFinalRowDefaultType;
                        } else {
                            finalSeatType = systemDefaultSeatType;
                        }
                        seat.setSeatType(finalSeatType);
                        seatsToCreate.add(seat);
                        totalCapacity++;
                    }
                } else {
                    log.warn("Service: Invalid row definition DTO skipped: {}", defDTO);
                }
            }
            
        } else {
             log.warn("No row definitions provided in DTO for room: {}", roomFormDTO.getName());
        }

        // Lưu Room Entity trước để có ID (nếu là mới)
        // Các thay đổi như name, active đã được set.
        // Capacity sẽ được set và lưu ở cuối.
        if (isNewRoom) {
             roomEntity = roomRepository.save(roomEntity);
             // Cập nhật room reference cho các Seat (nếu cần, mặc dù đã set ở trên,
             // nhưng nếu ID của roomEntity thay đổi sau khi save, cần cập nhật lại)
             final Room persistedRoom = roomEntity; // final cho lambda/stream
             seatsToCreate.forEach(seat -> seat.setRoom(persistedRoom));
        }


        roomEntity.setCapacity(totalCapacity);
        Room finalSavedRoom = roomRepository.save(roomEntity); // Lưu Room với capacity

        if (!seatsToCreate.isEmpty()) {
            // Nếu roomEntity không phải mới, ID đã có, seats đã được set room ref đúng.
            // Nếu roomEntity mới, ID đã được lấy sau lần save đầu, seats đã được cập nhật room ref.
            seatService.saveAll(seatsToCreate);
            log.info("Service: Created/Updated {} seats for room ID: {}", seatsToCreate.size(), finalSavedRoom.getId());
        } else {
            log.info("Service: No seats to create/update for room ID: {}", finalSavedRoom.getId());
        }

        log.info("Service: Room '{}' (ID: {}) finalized save/update with capacity {}. Layout contains {} seats.",
                 finalSavedRoom.getName(), finalSavedRoom.getId(), finalSavedRoom.getCapacity(), seatsToCreate.size());
        return finalSavedRoom;
    }
    
    private List<SeatAssignmentDTO> parseAndGroupFlatSeatAssignments(String flatSeatAssignmentsJson) {
        if (!StringUtils.hasText(flatSeatAssignmentsJson) || "null".equals(flatSeatAssignmentsJson.trim()) || "[]".equals(flatSeatAssignmentsJson.trim())) {
            log.warn("flatSeatAssignmentsJson is empty, null, or an empty array. No seat assignments to parse and group.");
            return Collections.emptyList();
        }
        List<Map<String, Object>> flatParsedList;
        try {
            flatParsedList = objectMapper.readValue(flatSeatAssignmentsJson, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parsing flatSeatAssignmentsJson: {}. JSON: {}", e.getMessage(), flatSeatAssignmentsJson.substring(0, Math.min(flatSeatAssignmentsJson.length(), 200)));
            return Collections.emptyList();
        }
        Map<String, List<SeatInAssignmentDTO>> groupedByRow = flatParsedList.stream()
            .collect(Collectors.groupingBy(
                map -> (String) map.get("rowIdentifier"),
                LinkedHashMap::new,
                Collectors.mapping(map -> {
                    SeatInAssignmentDTO seatInDto = new SeatInAssignmentDTO();
                    Object seatNumObj = map.get("seatNumber");
                    if (seatNumObj instanceof Integer) {
                        seatInDto.setSeatNumber((Integer) seatNumObj);
                    } else if (seatNumObj instanceof Number) {
                        seatInDto.setSeatNumber(((Number) seatNumObj).intValue());
                    } else {
                        log.warn("Invalid seatNumber format: {} for row {}", seatNumObj, map.get("rowIdentifier"));
                        return null;
                    }
                    Object seatTypeIdObj = map.get("seatTypeId");
                    if (seatTypeIdObj != null) {
                         if (seatTypeIdObj instanceof Integer) {
                            seatInDto.setSeatTypeId(((Integer) seatTypeIdObj).longValue());
                        } else if (seatTypeIdObj instanceof Long) {
                            seatInDto.setSeatTypeId((Long) seatTypeIdObj);
                        } else if (seatTypeIdObj instanceof String && !((String) seatTypeIdObj).isEmpty()){
                             try { seatInDto.setSeatTypeId(Long.parseLong((String) seatTypeIdObj)); }
                             catch (NumberFormatException ignored) {
                                 log.warn("Could not parse seatTypeId string: '{}' for row {}, seatNum {}", seatTypeIdObj, map.get("rowIdentifier"), seatNumObj);
                             }
                         }
                    }
                    return seatInDto;
                }, Collectors.toList())
            ));
        groupedByRow.forEach((key, value) -> value.removeIf(Objects::isNull));
        return groupedByRow.entrySet().stream()
            .map(entry -> {
                SeatAssignmentDTO assignmentDTO = new SeatAssignmentDTO();
                assignmentDTO.setRowIdentifier(entry.getKey());
                assignmentDTO.setSeats(entry.getValue());
                return assignmentDTO;
            })
            .sorted(Comparator.comparing(SeatAssignmentDTO::getRowIdentifier, yourCustomRowIdentifierComparator()))
            .collect(Collectors.toList());
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

    public boolean isColorDark(String colorCode) {
        // Logic của bạn để xác định màu tối
        // Ví dụ:
        if (colorCode == null || colorCode.isEmpty()) {
            return false; // Hoặc true, tùy theo logic mặc định của bạn
        }
        // Chuyển sang chữ thường để so sánh không phân biệt hoa thường
        String lowerColorCode = colorCode.toLowerCase();

        // Các mã màu tối phổ biến (ví dụ)
        Set<String> darkColors = Set.of("#000000", "black", "#333333", "darkblue");

        // Hoặc logic phức tạp hơn dựa trên giá trị RGB
        // Ví dụ: tính độ sáng (luminance)
        // int r = Integer.parseInt(colorCode.substring(1, 3), 16);
        // int g = Integer.parseInt(colorCode.substring(3, 5), 16);
        // int b = Integer.parseInt(colorCode.substring(5, 7), 16);
        // double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
        // return luminance < 0.5; // Ngưỡng độ sáng, bạn có thể điều chỉnh

        return darkColors.contains(lowerColorCode);
    }
}