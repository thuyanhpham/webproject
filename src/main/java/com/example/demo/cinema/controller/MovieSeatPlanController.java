package com.example.demo.cinema.controller;

import com.example.demo.cinema.dto.BookingRequestDTO;
import com.example.demo.cinema.dto.SeatMapDTO;
import com.example.demo.cinema.entity.Booking;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.service.BookingService;
import com.example.demo.cinema.service.SeatManagementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MovieSeatPlanController {

    private final SeatManagementService seatManagementService;
    private final BookingService bookingService;

    @Autowired
    public MovieSeatPlanController(SeatManagementService seatManagementService, BookingService bookingService) {
        this.seatManagementService = seatManagementService;
        this.bookingService = bookingService;
    }

    @GetMapping("/movie-seat-plan")
    public String showSeatPlanPage(@RequestParam Long showtimeId,
                                   Model model,
                                   HttpServletRequest request,
                                   RedirectAttributes redirectAttributes) {
        try {
            SeatMapDTO seatMap = seatManagementService.getSeatMapForShowtime(showtimeId);
            if (seatMap == null) {
                throw new ResourceNotFoundException("Could not load seat map for showtime ID: " + showtimeId);
            }

            model.addAttribute("seatMap", seatMap);
            model.addAttribute("pageTitle", seatMap.getMovieTitle() + " - Seat Selection");
            model.addAttribute("currentRequestURI", request.getRequestURI());
            model.addAttribute("showtimeIdForBooking", showtimeId);

            return "user/movie/movie-seat-plan";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/movies";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred while loading the seat plan.");
            return "redirect:/home";
        }
    }

    @PostMapping("/book-tickets")
    public String processBooking(@ModelAttribute BookingRequestDTO bookingRequest,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal UserDetails currentUser,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        Long showtimeId = bookingRequest.getShowtimeId();

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please login to book tickets.");
            return "redirect:/login?redirect=/movie-seat-plan?showtimeId=" + showtimeId;
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid booking request.");
            return "redirect:/movie-seat-plan?showtimeId=" + showtimeId;
        }

        try {
            Booking booking = bookingService.createBooking(
                    showtimeId,
                    bookingRequest.getSeatIds(),
                    currentUser.getUsername()
            );

            redirectAttributes.addFlashAttribute("successMessage", "Tickets booked successfully! Check your email for confirmation.");

            return "redirect:/booking/confirmation";
        } catch (IllegalArgumentException | ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/movie-seat-plan?showtimeId=" + showtimeId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred during booking. Please try again.");
            return "redirect:/movie-seat-plan?showtimeId=" + showtimeId;
        }
    }
}
