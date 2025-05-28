package com.example.demo.cinema.controller;

import com.example.demo.cinema.dto.SeatMapDTO;
import com.example.demo.cinema.dto.UpdateSeatStatusByAdminRequestDTO;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.service.SeatManagementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/showtimes")
public class SeatController {

    private final SeatManagementService seatManagementService;

    @Autowired
    public SeatController(SeatManagementService seatManagementService) {
        this.seatManagementService = seatManagementService;
    }

    @GetMapping("/{showtimeId}/seats")
    public String showAdminSeatManagementPage(
            @PathVariable Long showtimeId,
            Model model,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            SeatMapDTO seatMap = seatManagementService.getSeatMapForShowtime(showtimeId);
            model.addAttribute("seatMap", seatMap);
            model.addAttribute("showtimeId", showtimeId);

            // Vì seatMap không có getShowDateTime() nên ghép thủ công
            String showDateTime = seatMap.getShowFullDateDisplay() + " - " + seatMap.getShowStartTimeDisplay();
            model.addAttribute("pageTitle", "Manage Seats - " + seatMap.getMovieTitle() + " (" + showDateTime + ")");
            model.addAttribute("currentURI", request.getRequestURI());

            return "admin/showtime/seat-management";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/showtimes";
        }
    }

    @PostMapping("/{showtimeId}/seats/update-admin-status")
    @ResponseBody
    public ResponseEntity<?> updateSeatStatusByAdmin(
            @PathVariable Long showtimeId,
            @RequestBody UpdateSeatStatusByAdminRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails adminUserDetails) {

        if (adminUserDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Admin not authenticated."));
        }

        try {
            seatManagementService.updateSeatStatusesByAdmin(showtimeId, requestDTO, adminUserDetails.getUsername());
            return ResponseEntity.ok(Map.of("message", "Seat statuses updated successfully."));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not process request."));
        }
    }
}
