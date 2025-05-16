package com.example.demo.cinema.controller;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.entity.Room;
import com.example.demo.cinema.entity.Showtime;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.service.MovieService;
import com.example.demo.cinema.service.RoomService;
import com.example.demo.cinema.service.ShowtimeService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/admin/showtimes")
public class ShowtimeController {

    private static final Logger log = LoggerFactory.getLogger(ShowtimeController.class);

    private final ShowtimeService showtimeService;
    private final MovieService movieService;
    private final RoomService roomService;

    @Autowired
    public ShowtimeController(ShowtimeService showtimeService,
                              MovieService movieService,
                              RoomService roomService) {
        this.showtimeService = showtimeService;
        this.movieService = movieService;
        this.roomService = roomService;
    }

    @GetMapping("/movie/{movieId}")
    public String listShowtimesForMovie(
            @PathVariable Long movieId,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedDate,
            Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            Movie movie = movieService.getMovieById(movieId);
            model.addAttribute("movie", movie);
            List<LocalDate> availableDates = showtimeService.findDistinctShowDatesByMovieId(movieId);
            model.addAttribute("availableDates", availableDates);

            if (selectedDate == null && !availableDates.isEmpty()) {
                selectedDate = availableDates.get(0);
            }
            model.addAttribute("selectedDate", selectedDate);

            List<Showtime> showtimes;
            if (selectedDate != null) {
                showtimes = showtimeService.getShowtimesByMovieAndDate(movieId, selectedDate);
            } else {
                showtimes = List.of();
            }
            model.addAttribute("showtimes", showtimes);
            model.addAttribute("currentURI", request.getRequestURI());
            return "admin/showtime/list-by-movie";
        } catch (ResourceNotFoundException e) {
            log.error("Controller: Movie not found (ID: {}). {}", movieId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Movie not found.");
            return "redirect:/admin/movies";
        } catch (Exception e) {
            log.error("Controller: Error listing showtimes for movie ID {}: {}", movieId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading showtimes for the movie.");
            return "redirect:/admin/movie/details/" + movieId;
        }
    }

    @GetMapping("/movie/{movieId}/add")
    public String showAddShowtimeForm(@PathVariable Long movieId, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            Movie movie = movieService.getMovieById(movieId);
            Showtime showtime = new Showtime();
            showtime.setMovie(movie);
            showtime.setShowDate(LocalDate.now());
            List<Room> activeRooms = roomService.getAllActiveRooms();
            
            model.addAttribute("showtime", showtime);
            model.addAttribute("movieTitle", movie.getTitle());
            model.addAttribute("allRooms", activeRooms);
            model.addAttribute("currentURI", request.getRequestURI());
            return "admin/showtime/form";
        } catch (ResourceNotFoundException e) {
            log.error("Controller: Movie not found (ID: {}) when showing add showtime form. {}", movieId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Movie not found. Cannot add showtime.");
            return "redirect:/admin/movies";
        }
    }

    @GetMapping("/edit/{showtimeId}")
    public String showEditShowtimeForm(@PathVariable Long showtimeId, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            Showtime showtime = showtimeService.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + showtimeId));
            List<Room> activeRooms = roomService.getAllActiveRooms();
            
            model.addAttribute("showtime", showtime);
            model.addAttribute("movieTitle", showtime.getMovie().getTitle());
            model.addAttribute("allRooms", activeRooms);
            model.addAttribute("currentURI", request.getRequestURI());
            if (showtime.getStartTime() != null) {
                model.addAttribute("startTimeStringValue", showtime.getStartTime().toString());
            } else {
                model.addAttribute("startTimeStringValue", "");
            }
            return "admin/showtime/form";
        } catch (ResourceNotFoundException e) {
            log.error("Controller: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/showtimes";
        }
    }

    @PostMapping("/save")
    public String saveOrUpdateShowtime(@ModelAttribute("showtime") Showtime showtimeForm,
                                     BindingResult bindingResult,
                                     @RequestParam("movieId") Long movieId,
                                     @RequestParam("roomId") Long roomId,
                                     @RequestParam("showDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate showDate,
                                     @RequestParam("startTimeString") String startTimeString,
                                     @RequestParam(value = "experience", required = false) String experience,
                                     @RequestParam(value = "price", required = false) BigDecimal price,
                                     Model model,
                                     RedirectAttributes redirectAttributes,
                                     HttpServletRequest request) {

        try {
            Movie movie = movieService.getMovieById(movieId); 
            showtimeForm.setMovie(movie);
        } catch (ResourceNotFoundException e){
             bindingResult.reject("movieId", "Movie with ID " + movieId + " not found.");
        }
        try {
            Room room = roomService.getRoomById(roomId);
            showtimeForm.setRoom(room);
        } catch (ResourceNotFoundException e) {
            bindingResult.reject("roomId", "Room with ID " + roomId + " not found.");
        }
        showtimeForm.setShowDate(showDate);
        showtimeForm.setExperience(experience);
        showtimeForm.setPrice(price);

        if (startTimeString == null || startTimeString.isBlank()) {
            bindingResult.rejectValue("startTime", "showtime.startTime.notBlank", "Start time is required.");
        } else {
            try {
                LocalTime.parse(startTimeString);
                showtimeForm.setStartTime(LocalTime.parse(startTimeString));
            } catch (Exception e) {
                bindingResult.rejectValue("startTime", "showtime.startTime.invalidFormat", "Invalid start time format (HH:mm).");
            }
        }

        if (bindingResult.hasErrors()) {
            log.error("Controller: Validation errors for showtime: {}", bindingResult.getAllErrors());
            model.addAttribute("movieTitle", showtimeForm.getMovie() != null ? showtimeForm.getMovie().getTitle() : "Unknown Movie");
            model.addAttribute("allRooms", roomService.getAllActiveRooms());
            model.addAttribute("currentURI", request.getRequestURI());
            model.addAttribute("startTimeStringValue", startTimeString);
            return "admin/showtime/form";
        }

        try {

            if (showtimeForm.getId() == null) {
                log.debug("Controller: Calling createShowtime service...");
                showtimeService.createShowtime(movieId, roomId, showDate, startTimeString, experience, price);
            } else { 
                log.debug("Controller: Calling updateShowtime service for ID: {}", showtimeForm.getId());
                showtimeService.updateShowtime(showtimeForm.getId(), movieId, roomId, showDate, startTimeString, experience, price);
            }
            return "redirect:/admin/showtimes/movie/" + movieId + (showDate != null ? "?date=" + showDate.toString() : "");

        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            log.error("Controller: Error during showtime operation: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("movieTitle", showtimeForm.getMovie() != null ? showtimeForm.getMovie().getTitle() : "Unknown Movie");
            model.addAttribute("allRooms", roomService.getAllActiveRooms());
            model.addAttribute("currentURI", request.getRequestURI());
            model.addAttribute("startTimeStringValue", startTimeString);
            return "admin/showtime/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteShowtime(@PathVariable Long id,
                               @RequestParam(value = "movieId", required = false) Long movieId,
                               @RequestParam(value = "returnDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
                               RedirectAttributes redirectAttributes) {
        log.warn("Controller: Attempting to delete showtime with id: {}", id);
        try {
            showtimeService.deleteShowtime(id);
        } catch (Exception e) {
             log.error("Controller: Error deleting showtime with id {}: {}", id, e.getMessage(), e);
             redirectAttributes.addFlashAttribute("errorMessage", "Error deleting showtime: " + e.getMessage());
        }
        if (movieId != null) {
            String returnUrl = "redirect:/admin/showtimes/movie/" + movieId;
            if (returnDate != null) { returnUrl += "?date=" + returnDate.toString(); }
            return returnUrl;
        }
        return "redirect:/admin/showtimes";
    }
}