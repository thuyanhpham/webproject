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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoomService {

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
        return roomRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }
    
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    }
    
    public Optional<Room> findRoomOptionalById(Long id) {
        return roomRepository.findById(id);
    }

    public Room findByIdOrThrow(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("Room not found with id: " + id);
                });
    }
    
    public RoomFormDTO getRoomFormDTOForEdit(Long roomId) {
        Room roomEntity = findByIdOrThrow(roomId);
        RoomFormDTO roomForm = new RoomFormDTO();
        roomForm.setId(roomEntity.getId());
        roomForm.setName(roomEntity.getName());
        roomForm.setActive(roomEntity.isActive());

        List<Seat> seatsInRoom = seatService.findByRoomIdOrderByRowOrderAscSeatNumberAsc(roomId);

        List<RowDefinitionDTO> rowDefinitionDTOs = new ArrayList<>();
        Map<String, List<SeatInAssignmentDTO>> seatAssignmentsForJsonMap = new LinkedHashMap<>();

        if (!seatsInRoom.isEmpty()) {
            Map<String, Map<Integer, List<Seat>>> seatsGroupedByRowAndOrder = seatsInRoom.stream()
                .collect(Collectors.groupingBy(
                    Seat::getRowIdentifier,
                    LinkedHashMap::new,
                    Collectors.groupingBy(
                        Seat::getRowOrder,
                        LinkedHashMap::new, 
                        Collectors.toList()
                    )
                ));
            
            List<Map.Entry<String, Map<Integer, List<Seat>>>> sortedRows = new ArrayList<>(seatsGroupedByRowAndOrder.entrySet());
            sortedRows.sort(Comparator.comparing(entry ->
                entry.getValue().keySet().stream().min(Integer::compareTo).orElse(Integer.MAX_VALUE)
            ));

            for (Map.Entry<String, Map<Integer, List<Seat>>> rowEntry : sortedRows) {
                String rowIdentifier = rowEntry.getKey();
                Integer rowOrder = rowEntry.getValue().keySet().stream().findFirst().orElse(0);
                List<Seat> seatsInThisLogicalRow = rowEntry.getValue().values().stream().flatMap(List::stream)
                                                    .sorted(Comparator.comparingInt(Seat::getSeatNumber))
                                                    .collect(Collectors.toList());

                if (!seatsInThisLogicalRow.isEmpty()) {
                    RowDefinitionDTO defDTO = new RowDefinitionDTO();
                    defDTO.setIdentifier(rowIdentifier);
                    defDTO.setNumberOfSeats(seatsInThisLogicalRow.size());
                    defDTO.setOrder(rowOrder);
                    Long commonSeatTypeId = null;
                    if (!seatsInThisLogicalRow.isEmpty() && seatsInThisLogicalRow.get(0).getSeatType() != null) {
                        commonSeatTypeId = seatsInThisLogicalRow.get(0).getSeatType().getId();
                        boolean allSame = true;
                        for (Seat seat : seatsInThisLogicalRow) {
                            if (seat.getSeatType() == null || !commonSeatTypeId.equals(seat.getSeatType().getId())) {
                                allSame = false;
                                break;
                            }
                        }
                        if (!allSame) {
                            commonSeatTypeId = null;
                        }
                    }
                    defDTO.setSeatTypeId(commonSeatTypeId);
                    rowDefinitionDTOs.add(defDTO);

                    List<SeatInAssignmentDTO> seatsInAssignmentRow = seatsInThisLogicalRow.stream()
                        .map(seat -> {
                            SeatInAssignmentDTO saDto = new SeatInAssignmentDTO();
                            saDto.setSeatNumber(seat.getSeatNumber());
                            saDto.setSeatTypeId(seat.getSeatType() != null ? seat.getSeatType().getId() : null);
                            return saDto;
                        })
                        .collect(Collectors.toList());
                    seatAssignmentsForJsonMap.computeIfAbsent(rowIdentifier, k -> new ArrayList<>()).addAll(seatsInAssignmentRow);
                }
            }
        }
        roomForm.setRowDefinitions(rowDefinitionDTOs);

        List<SeatAssignmentDTO> seatAssignmentsListForJsonFinal = seatAssignmentsForJsonMap.entrySet().stream()
            .map(entry -> {
                SeatAssignmentDTO saDto = new SeatAssignmentDTO();
                saDto.setRowIdentifier(entry.getKey());
                saDto.setSeats(entry.getValue());
                return saDto;
            })
            .collect(Collectors.toList());
             seatAssignmentsListForJsonFinal.sort(Comparator.comparing(sa ->
                rowDefinitionDTOs.stream()
                    .filter(rd -> rd.getIdentifier().equals(sa.getRowIdentifier()))
                    .findFirst()
                    .map(RowDefinitionDTO::getOrder)
                    .orElse(Integer.MAX_VALUE)
            ));


        try {
            roomForm.setSeatAssignmentsJson(objectMapper.writeValueAsString(seatAssignmentsListForJsonFinal));
        } catch (JsonProcessingException e) {
            roomForm.setSeatAssignmentsJson("[]"); 
        }
        return roomForm;
    }

    @Transactional
    public Room saveRoomAndLayout(RoomFormDTO roomFormDTO) {
        Room roomEntity;
        boolean isNewRoom = (roomFormDTO.getId() == null || roomFormDTO.getId() == 0L);

        if (isNewRoom) {
            roomEntity = new Room();
        } else {
            roomEntity = findByIdOrThrow(roomFormDTO.getId());
            seatService.deleteByRoomId(roomEntity.getId()); 
        }

        roomEntity.setName(roomFormDTO.getName());
        roomEntity.setActive(roomFormDTO.isActive());
        List<SeatAssignmentDTO> seatAssignmentsNested = parseAndGroupFlatSeatAssignments(roomFormDTO.getSeatAssignmentsJson());
        Map<String, Map<Integer, Long>> clientAssignedSeatTypes = new HashMap<>();
        if (seatAssignmentsNested != null) {
            for (SeatAssignmentDTO rowAssignment : seatAssignmentsNested) {
                Map<Integer, Long> seatsInRowMap = new HashMap<>();
                if (rowAssignment.getSeats() != null) {
                    for (SeatInAssignmentDTO seatInAssignment : rowAssignment.getSeats()) {
                        seatsInRowMap.put(seatInAssignment.getSeatNumber(), seatInAssignment.getSeatTypeId());
                    }
                }
                clientAssignedSeatTypes.put(rowAssignment.getRowIdentifier().trim().toUpperCase(), seatsInRowMap);
            }
        }

        List<Seat> seatsToCreate = new ArrayList<>();
        int totalCapacity = 0;
        List<RowDefinitionDTO> rowDefinitionsFromClient = roomFormDTO.getRowDefinitions();

        final SeatType systemDefaultSeatType = seatTypeRepository.findByName(DEFAULT_SEAT_TYPE_NAME_FOR_NEW_SEATS)
            .or(() -> seatTypeRepository.findFirstByIsActiveTrue())
            .orElseThrow(() -> new IllegalStateException("FATAL: Default or any active SeatType not found."));

        if (rowDefinitionsFromClient != null && !rowDefinitionsFromClient.isEmpty()) {
            rowDefinitionsFromClient.sort(Comparator.comparing(RowDefinitionDTO::getOrder, Comparator.nullsLast(Integer::compareTo))
                                                   .thenComparing(RowDefinitionDTO::getIdentifier, yourCustomRowIdentifierComparator()));

            for (RowDefinitionDTO defDTO : rowDefinitionsFromClient) {
                if (isValidRowDefinition(defDTO)) {
                    String rowId = defDTO.getIdentifier().trim().toUpperCase();
                    Integer rowOrder = defDTO.getOrder();

                    final SeatType rowDefaultTypeFromClientDTO;
                    if (defDTO.getSeatTypeId() != null) {
                        rowDefaultTypeFromClientDTO = seatTypeRepository.findById(defDTO.getSeatTypeId()).orElse(null);
                        if (rowDefaultTypeFromClientDTO == null) {
                        }
                    } else {
                        rowDefaultTypeFromClientDTO = null;
                    }

                    Map<Integer, Long> specificAssignmentsForThisRow = clientAssignedSeatTypes.getOrDefault(rowId, Collections.emptyMap());

                    for (int i = 1; i <= defDTO.getNumberOfSeats(); i++) {
                        final int currentSeatNumber = i;
                        Seat seat = new Seat();
                        seat.setRoom(roomEntity); 
                        seat.setRowIdentifier(rowId);
                        seat.setSeatNumber(currentSeatNumber);
                        seat.setRowOrder(rowOrder);
                        seat.setSeatLabel(rowId + currentSeatNumber);
                        seat.setAvailable(true);
                        seat.setActive(true);

                        Long specificSeatTypeId = specificAssignmentsForThisRow.get(currentSeatNumber);
                        SeatType finalSeatType;

                        if (specificSeatTypeId != null) { 
                            final Long capturedId = specificSeatTypeId;
                            finalSeatType = seatTypeRepository.findById(capturedId)
                                .orElseGet(() -> {
                                    return rowDefaultTypeFromClientDTO != null ? rowDefaultTypeFromClientDTO : systemDefaultSeatType;
                                });
                        } else if (rowDefaultTypeFromClientDTO != null) { 
                            finalSeatType = rowDefaultTypeFromClientDTO;
                        } else {
                            finalSeatType = systemDefaultSeatType;
                        }
                        seat.setSeatType(finalSeatType);
                        seatsToCreate.add(seat);
                        totalCapacity++;
                    }
                }
            }
        } 

        if (isNewRoom) {
            roomEntity = roomRepository.save(roomEntity);
            final Room persistedRoom = roomEntity;
            seatsToCreate.forEach(seat -> seat.setRoom(persistedRoom));
        }
        roomEntity.setCapacity(totalCapacity);
        Room finalSavedRoom = roomRepository.save(roomEntity);
        if (!seatsToCreate.isEmpty()) {
            seatService.saveAll(seatsToCreate);
        }
        return finalSavedRoom;
    }
    
    private List<SeatAssignmentDTO> parseAndGroupFlatSeatAssignments(String flatSeatAssignmentsJson) {
        if (!StringUtils.hasText(flatSeatAssignmentsJson) || "null".equals(flatSeatAssignmentsJson.trim()) || "[]".equals(flatSeatAssignmentsJson.trim())) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> flatParsedList;
        try {
            flatParsedList = objectMapper.readValue(flatSeatAssignmentsJson, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
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
                        return null;
                    }
                    Object seatTypeIdObj = map.get("seatTypeId");
                    if (seatTypeIdObj != null) {
                         if (seatTypeIdObj instanceof Integer) {
                            seatInDto.setSeatTypeId(((Integer) seatTypeIdObj).longValue());
                        } else if (seatTypeIdObj instanceof Long) {
                            seatInDto.setSeatTypeId((Long) seatTypeIdObj);
                        } else if (seatTypeIdObj instanceof String && !((String) seatTypeIdObj).isEmpty()){
                            seatInDto.setSeatTypeId(Long.parseLong((String) seatTypeIdObj));
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
            throw new ResourceNotFoundException("Room not found with id: " + id + " for deletion.");
        }
        seatService.deleteByRoomId(id);
        roomRepository.deleteById(id);
    }

    private boolean isValidRowDefinition(RowDefinitionDTO def) {
        return def != null &&
               def.getIdentifier() != null && !def.getIdentifier().trim().isEmpty() &&
               def.getNumberOfSeats() != null && def.getNumberOfSeats() > 0 &&
               def.getOrder() != null && def.getOrder() >= 0;
    }

    private Comparator<String> yourCustomRowIdentifierComparator() {
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
        if (colorCode == null || colorCode.isEmpty()) {
            return false; 
        }
        String lowerColorCode = colorCode.toLowerCase();
        Set<String> darkColors = Set.of("#000000", "black", "#333333", "darkblue");
        return darkColors.contains(lowerColorCode);
    }
}