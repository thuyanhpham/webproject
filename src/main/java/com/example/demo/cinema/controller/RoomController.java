package com.example.demo.cinema.controller;

import com.example.demo.cinema.dto.RoomFormDTO;
import com.example.demo.cinema.dto.RowDefinitionDTO;
import com.example.demo.cinema.entity.Room;
import com.example.demo.cinema.entity.Seat;
import com.example.demo.cinema.entity.SeatType;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.service.RoomService;
import com.example.demo.cinema.service.SeatService;
import com.example.demo.cinema.service.SeatTypeService;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/rooms")
public class RoomController {

    private static final Logger log = LoggerFactory.getLogger(RoomController.class);

    private final RoomService roomService;
    private final SeatService seatService;
    private final SeatTypeService seatTypeService;

    @Autowired
    public RoomController(RoomService roomService, SeatService seatService, SeatTypeService seatTypeService) {
        this.roomService = roomService;
        this.seatService = seatService;
        this.seatTypeService = seatTypeService;
    }

    /**
     * Thêm các thuộc tính chung cần thiết cho các view form (tạo mới, sửa cấu trúc).
     */
    private void addCommonFormAttributesToModel(Model model, HttpServletRequest request) {
        model.addAttribute("currentURI", request.getRequestURI());
        // Chỉ thêm availableSeatTypes nếu view này thực sự cần (ví dụ: form.html nếu có chọn seat type cho hàng)
        // Trang details/layout sẽ được nạp riêng.
        // Nếu form.html không có chọn seat type cho hàng, dòng dưới có thể không cần ở đây.
        // model.addAttribute("availableSeatTypes", seatTypeService.findAllActive());
    }

    /**
     * Thêm các thuộc tính chung cần thiết cho view details/layout manager.
     */
    private void addDetailsLayoutAttributesToModel(Model model, HttpServletRequest request, Room room, List<SeatType> availableSeatTypes, Map<String, List<Seat>> sortedGroupedSeatsByRow) {
        model.addAttribute("room", room);
        model.addAttribute("availableSeatTypes", availableSeatTypes);
        model.addAttribute("groupedSeatsByRow", sortedGroupedSeatsByRow);
        model.addAttribute("currentURI", request.getRequestURI());
    }


    @GetMapping
    public String listRooms(Model model, HttpServletRequest request) {
        log.info("Controller: Request to list all rooms");
        model.addAttribute("rooms", roomService.getAllRoomsOrderedByName());
        model.addAttribute("currentURI", request.getRequestURI()); // Cho menu
        return "admin/rooms/list";
    }

    @GetMapping("/new")
    public String showCreateRoomForm(Model model, HttpServletRequest request) {
        log.info("Controller: Request to show create new room form");
        RoomFormDTO roomForm = new RoomFormDTO();
        RowDefinitionDTO defaultRow = new RowDefinitionDTO();
        defaultRow.setIdentifier("A");
        defaultRow.setNumberOfSeats(10);
        defaultRow.setOrder(1);
        roomForm.getRowDefinitions().add(defaultRow);

        model.addAttribute("roomForm", roomForm);
        addCommonFormAttributesToModel(model, request);
        // Nếu form.html CÓ dropdown chọn SeatType cho mỗi RowDefinition, thì phải thêm ở đây:
        // model.addAttribute("availableSeatTypes", seatTypeService.findAllActive());
        return "admin/rooms/form";
    }

    @GetMapping("/edit/{roomId}")
    public String showEditRoomStructureForm(@PathVariable Long roomId, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.info("Controller: Request to show edit room STRUCTURE form for room ID: {}", roomId);
        try {
            RoomFormDTO roomForm = roomService.getRoomFormDTOForEdit(roomId);
            model.addAttribute("roomForm", roomForm);
            addCommonFormAttributesToModel(model, request);
            // Tương tự như /new, nếu form.html CÓ dropdown chọn SeatType:
            // model.addAttribute("availableSeatTypes", seatTypeService.findAllActive());
            return "admin/rooms/form";
        } catch (ResourceNotFoundException e) {
            log.warn("Controller: Room not found for editing structure, ID: {}. Redirecting. Error: {}", roomId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/rooms";
        }
    }

    @PostMapping("/save")
    public String saveRoomStructure(@ModelAttribute("roomForm") RoomFormDTO roomForm,
                           BindingResult bindingResult, Model model,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {
        log.info("Controller: Attempting to save room structure. RoomFormDTO Name: '{}', ID: {}", roomForm.getName(), roomForm.getId());

        if (bindingResult.hasErrors()) {
            log.warn("Controller: Validation errors found when saving room structure. Error count: {}", bindingResult.getErrorCount());
            addCommonFormAttributesToModel(model, request);
            // Nếu form.html CÓ dropdown chọn SeatType:
            // model.addAttribute("availableSeatTypes", seatTypeService.findAllActive());
            return "admin/rooms/form";
        }

        try {
            Room savedRoom = roomService.saveRoomAndLayout(roomForm);
            redirectAttributes.addFlashAttribute("successMessage",
                "Room '" + savedRoom.getName() + "' structure has been " + (roomForm.getId() == null ? "created" : "updated") + " successfully!");
            log.info("Controller: Room structure save/update successful for name: '{}', redirecting to layout manager.", savedRoom.getName());
            return "redirect:/admin/rooms/" + savedRoom.getId() + "/layout-manager"; // Chuyển đến trang quản lý layout
        } catch (ResourceNotFoundException e) {
            log.error("Controller: Error saving room structure - Resource not found: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Controller: Error saving room structure - Data integrity violation: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Could not save room. Data conflict or constraint violation: " + e.getMostSpecificCause().getMessage());
        } catch (IllegalStateException e) {
             log.error("Controller: Error saving room structure - Configuration issue (e.g., default SeatType not found): {}", e.getMessage(), e);
             model.addAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Controller: Unexpected error saving room structure: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "An unexpected error occurred while saving the room structure. Please try again.");
        }
        log.warn("Controller: Re-displaying room structure form for room: '{}' due to error during save process.", roomForm.getName());
        addCommonFormAttributesToModel(model, request);
        // Nếu form.html CÓ dropdown chọn SeatType:
        // model.addAttribute("availableSeatTypes", seatTypeService.findAllActive());
        return "admin/rooms/form";
    }

    
    @GetMapping("/details/{roomId}")
    public String showRoomDetailsAndLayoutManager(@PathVariable Long roomId, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.info("Controller: Request to manage layout for room ID: {}", roomId);
        try {
            Room room = roomService.findByIdOrThrow(roomId);
            List<SeatType> activeSeatTypes = seatTypeService.findAllActive(); // Lấy các loại ghế đang active
            List<Seat> seatsInRoom = seatService.findByRoomIdOrderByRowIdentifierAscSeatNumberAsc(roomId);

            Map<String, List<Seat>> groupedSeatsByRow = seatsInRoom.stream()
                .collect(Collectors.groupingBy(
                    Seat::getRowIdentifier,
                    LinkedHashMap::new,
                    Collectors.toList()
                ));

            Map<String, List<Seat>> sortedGroupedSeatsByRow = new LinkedHashMap<>();
            groupedSeatsByRow.keySet().stream()
                .sorted(yourCustomRowIdentifierComparator())
                .forEach(key -> sortedGroupedSeatsByRow.put(key, groupedSeatsByRow.get(key)));

            // Sử dụng helper method để thêm attribute
            addDetailsLayoutAttributesToModel(model, request, room, activeSeatTypes, sortedGroupedSeatsByRow);

            return "admin/rooms/details"; // Tên file HTML cho trang này
        } catch (ResourceNotFoundException e) {
            log.warn("Controller: Room not found for layout management, ID: {}. Redirecting. Error: {}", roomId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/rooms";
        }
    }

    @PostMapping("/{roomId}/seats/{seatId}/update-type")
    @ResponseBody
    public ResponseEntity<?> updateSeatType(@PathVariable Long roomId,
                                            @PathVariable Long seatId,
                                            @RequestParam(required = false) Long seatTypeId) { // Bỏ RedirectAttributes vì không dùng
        log.info("Controller: API Request to update SeatType for seat ID: {} in room ID: {} to SeatType ID: {}", seatId, roomId, seatTypeId);
        try {
            Seat updatedSeat = seatService.updateSeatType(seatId, seatTypeId);
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Seat type updated successfully.");
            responseBody.put("seatId", updatedSeat.getId());
            if (updatedSeat.getSeatType() != null) {
                responseBody.put("newSeatTypeId", updatedSeat.getSeatType().getId());
                responseBody.put("newSeatTypeName", updatedSeat.getSeatType().getName());
                responseBody.put("newSeatTypeColor", updatedSeat.getSeatType().getColor());
            } else {
                responseBody.put("newSeatTypeId", null);
                responseBody.put("newSeatTypeName", "Unassigned");
                responseBody.put("newSeatTypeColor", "#4a5a80");
            }
            return ResponseEntity.ok().body(responseBody);
        } catch (ResourceNotFoundException e) {
            log.warn("Controller API: Error updating seat type. Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Controller API: Error updating seat type for seat ID {}: {}", seatId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }

    @PostMapping("/delete/{roomId}")
    public String deleteRoom(@PathVariable Long roomId, RedirectAttributes redirectAttributes) {
        log.info("Controller: Request to delete room ID: {}", roomId);
        try {
            roomService.deleteRoomById(roomId);
            redirectAttributes.addFlashAttribute("successMessage", "Room deleted successfully!");
        } catch (ResourceNotFoundException e) {
            log.warn("Controller: Room not found for deletion, ID: {}. Error: {}", roomId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Controller: Error deleting room (data integrity violation) ID {}: {}", roomId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete room. It might be currently in use (e.g., by showtimes).");
        }
         catch (Exception e) {
            log.error("Controller: Unexpected error deleting room ID {}: {}", roomId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while deleting the room.");
        }
        return "redirect:/admin/rooms";
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
}