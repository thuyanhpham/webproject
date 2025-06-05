package com.example.demo.cinema.controller;

import com.example.demo.cinema.dto.RoomFormDTO;
// ... (các import khác giữ nguyên như bạn đã có)
import com.example.demo.cinema.entity.Room;
import com.example.demo.cinema.entity.Seat;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.service.RoomService;
import com.example.demo.cinema.service.SeatService;
import com.example.demo.cinema.service.SeatTypeService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Quan trọng
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
// import java.util.Objects; // Bạn có thể không cần Objects nếu không dùng trực tiếp
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/rooms")
public class RoomController {

    private static final Logger log = LoggerFactory.getLogger(RoomController.class);

    private final RoomService roomService;
    private final SeatService seatService;
    private final SeatTypeService seatTypeService;
    private final ObjectMapper objectMapper;

    @Autowired
    public RoomController(RoomService roomService,
                          SeatService seatService,
                          SeatTypeService seatTypeService,
                          ObjectMapper objectMapper) {
        this.roomService = roomService;
        this.seatService = seatService;
        this.seatTypeService = seatTypeService;
        this.objectMapper = objectMapper;
    }

    private void addCommonFormAttributesToModel(Model model, HttpServletRequest request) {
        model.addAttribute("currentURI", request.getRequestURI());
        model.addAttribute("availableSeatTypes", seatTypeService.findAllActive());
        // === THAY ĐỔI CHÍNH Ở ĐÂY ===
        model.addAttribute("roomService", this.roomService);
        // ===========================
    }

    private void addDetailsPageAttributesToModel(Model model, HttpServletRequest request, Room room, Map<String, List<Seat>> finalGroupedSeatsByRow) {
        model.addAttribute("room", room);
        model.addAttribute("groupedSeatsByRow", finalGroupedSeatsByRow);
        model.addAttribute("currentURI", request.getRequestURI());
        // Nếu trang details cũng cần isColorDark, bạn cũng cần thêm roomService vào đây
        // model.addAttribute("roomService", this.roomService);
    }

    @GetMapping
    public String listRooms(Model model, HttpServletRequest request) {
        log.info("Controller: Request to list all rooms");
        model.addAttribute("rooms", roomService.getAllRoomsOrderedByName());
        model.addAttribute("currentURI", request.getRequestURI());
        return "admin/rooms/list";
    }

    @GetMapping("/new")
    public String showCreateRoomForm(Model model, HttpServletRequest request) {
        log.info("Controller: Request to show create new room form");
        RoomFormDTO roomForm = new RoomFormDTO();
        roomForm.setRowDefinitions(new ArrayList<>());
        roomForm.setSeatAssignmentsJson("[]");
        model.addAttribute("roomForm", roomForm);
        addCommonFormAttributesToModel(model, request); // Sẽ gọi phương thức đã được cập nhật
        return "admin/rooms/form";
    }

    @GetMapping("/edit/{roomId}")
    public String showEditRoomForm(@PathVariable Long roomId, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.info("Controller: Request to show edit room form for room ID: {}", roomId);
        try {
            RoomFormDTO roomForm = roomService.getRoomFormDTOForEdit(roomId);
            model.addAttribute("roomForm", roomForm);
            addCommonFormAttributesToModel(model, request); // Sẽ gọi phương thức đã được cập nhật
            return "admin/rooms/form";
        } catch (ResourceNotFoundException e) {
            log.warn("Controller: Room not found for editing, ID: {}. Redirecting. Error: {}", roomId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/rooms";
        }
    }

    @PostMapping("/save")
    public String saveRoomAndLayout(@ModelAttribute("roomForm") RoomFormDTO roomForm,
                                  BindingResult bindingResult,
                                  @RequestParam(name = "seatAssignmentsJson", required = false) String seatAssignmentsJsonString,
                                  Model model,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            addCommonFormAttributesToModel(model, request); // Quan trọng khi có lỗi validation
            return "admin/rooms/form";
        }

        log.info("Controller: Received seatAssignmentsJsonString: {}", seatAssignmentsJsonString);

        if (seatAssignmentsJsonString != null && !seatAssignmentsJsonString.trim().isEmpty() && !seatAssignmentsJsonString.trim().equals("null") && !seatAssignmentsJsonString.trim().equals("[]")) {
            roomForm.setSeatAssignmentsJson(seatAssignmentsJsonString);
        } else {
            roomForm.setSeatAssignmentsJson("[]");
            log.info("Controller: seatAssignmentsJsonString is null or empty. Setting empty JSON array to RoomFormDTO.");
        }

        if (roomForm.getRowDefinitions() == null) {
            roomForm.setRowDefinitions(new ArrayList<>());
            log.warn("Controller: roomForm.rowDefinitions was null, initialized to empty list.");
        }

        try {
            Room savedRoom = roomService.saveRoomAndLayout(roomForm);
            redirectAttributes.addFlashAttribute("successMessage",
                "Room '" + savedRoom.getName() + "' and its layout have been " + (roomForm.getId() == null ? "created" : "updated") + " successfully!");
            log.info("Controller: Room and layout save/update successful for name: '{}', redirecting to details page for ID: {}.", savedRoom.getName(), savedRoom.getId());
            return "redirect:/admin/rooms/details/" + savedRoom.getId();
        } catch (ResourceNotFoundException | IllegalStateException | IllegalArgumentException e) {
            log.error("Controller: Error saving room - {}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            model.addAttribute("errorMessage", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            log.error("Controller: Error saving room - Data integrity violation: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Could not save room. Data conflict or constraint violation: " + e.getMostSpecificCause().getMessage());
        } catch (Exception e) {
            log.error("Controller: Unexpected error saving room: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "An unexpected error occurred while saving the room. Please try again.");
        }

        log.warn("Controller: Re-displaying room form for room: '{}' due to error during save process.", roomForm.getName());
        addCommonFormAttributesToModel(model, request); // Quan trọng khi có lỗi và quay lại form
        return "admin/rooms/form";
    }


    @GetMapping("/details/{roomId}")
    public String showRoomDetailsWithFinalLayout(@PathVariable Long roomId, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.info("Controller: Request to show room details and final layout for room ID: {}", roomId);
        try {
            Room room = roomService.findByIdOrThrow(roomId);
            List<Seat> seatsInRoom = seatService.findByRoomIdOrderByRowOrderAscSeatNumberAsc(roomId);

            Map<String, List<Seat>> groupedSeatsByRow = seatsInRoom.stream()
                .collect(Collectors.groupingBy(
                    Seat::getRowIdentifier,
                    LinkedHashMap::new,
                    Collectors.toList()
                ));

            addDetailsPageAttributesToModel(model, request, room, groupedSeatsByRow);
            // Nếu trang details cũng cần dùng isColorDark, ví dụ để hiển thị màu chữ trên màu nền của seat type
            // thì cũng cần thêm roomService vào model ở addDetailsPageAttributesToModel
            // hoặc thêm trực tiếp ở đây: model.addAttribute("roomService", this.roomService);
            return "admin/rooms/details";
        } catch (ResourceNotFoundException e) {
            log.warn("Controller: Room not found for details page, ID: {}. Redirecting. Error: {}", roomId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/rooms";
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
}

