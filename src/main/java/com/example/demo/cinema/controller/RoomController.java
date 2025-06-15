package com.example.demo.cinema.controller;

import com.example.demo.cinema.dto.RoomFormDTO;
import com.example.demo.cinema.dto.SeatTypeDTO;
import com.example.demo.cinema.entity.Room;
import com.example.demo.cinema.entity.SeatType;
import com.example.demo.cinema.service.RoomService;
import com.example.demo.cinema.service.SeatService;
import com.example.demo.cinema.service.SeatTypeService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Controller
@RequestMapping("/admin/rooms")
public class RoomController {

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
        List<SeatType> seatTypeEntities = seatTypeService.findAllActive();
        List<SeatTypeDTO> seatTypeDTOs = seatTypeEntities.stream()
            .map(entity -> new SeatTypeDTO( // Đảm bảo gọi đúng constructor với đủ tham số
                entity.getId(),
                entity.getName() != null ? entity.getName() : "Unnamed Type " + entity.getId(),
                entity.getColor(),
                entity.isCouple(),
                entity.getPrice() 
            ))
            .collect(Collectors.toList());
        model.addAttribute("availableSeatTypes", seatTypeDTOs); // Đây là biến sẽ được Thymeleaf inline
    }

    @GetMapping
    public String listRooms(Model model, HttpServletRequest request) {
        model.addAttribute("rooms", roomService.getAllRoomsOrderedByName());
        model.addAttribute("currentURI", request.getRequestURI());
        return "admin/rooms/list";
    }

    @GetMapping("/new")
    public String showCreateRoomForm(Model model, HttpServletRequest request) {
        RoomFormDTO roomForm = new RoomFormDTO();
        roomForm.setRowDefinitions(new ArrayList<>());
        roomForm.setSeatAssignmentsJson("[]");
        model.addAttribute("roomForm", roomForm);
        addCommonFormAttributesToModel(model, request);
        return "admin/rooms/form";
    }

    @GetMapping("/edit/{roomId}")
    public String showEditRoomForm(@PathVariable Long roomId, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
            RoomFormDTO roomForm = roomService.getRoomFormDTOForEdit(roomId);
            model.addAttribute("roomForm", roomForm);
            addCommonFormAttributesToModel(model, request);
            return "admin/rooms/form";
    }

    @PostMapping("/save")
    public String saveRoomAndLayout(@ModelAttribute("roomForm") RoomFormDTO roomForm,
                                  BindingResult bindingResult,
                                  @RequestParam(name = "seatAssignmentsJson", required = false) String seatAssignmentsJsonString,
                                  Model model,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            addCommonFormAttributesToModel(model, request); 
            return "admin/rooms/form";
        }

        if (seatAssignmentsJsonString != null && !seatAssignmentsJsonString.trim().isEmpty() && !seatAssignmentsJsonString.trim().equals("null") && !seatAssignmentsJsonString.trim().equals("[]")) {
            roomForm.setSeatAssignmentsJson(seatAssignmentsJsonString);
        } else {
            roomForm.setSeatAssignmentsJson("[]");
        }

        if (roomForm.getRowDefinitions() == null) {
            roomForm.setRowDefinitions(new ArrayList<>());
        }

        try {
            Room savedRoom = roomService.saveRoomAndLayout(roomForm);
            return "redirect:/admin/rooms"; 
        } catch (Exception e) {
            addCommonFormAttributesToModel(model, request); 
            return "admin/rooms/form";
        }
}

    @PostMapping("/delete/{roomId}")
    public String deleteRoom(@PathVariable Long roomId, RedirectAttributes redirectAttributes) {
        roomService.deleteRoomById(roomId);
        return "redirect:/admin/rooms";
    }
}

