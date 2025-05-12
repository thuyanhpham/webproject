package com.example.demo.cinema.controller;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.entity.Room;
import com.example.demo.cinema.entity.Showtime;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.service.MovieService;
import com.example.demo.cinema.service.RoomService;
import com.example.demo.cinema.service.ShowtimeService;

import jakarta.servlet.http.HttpServletRequest;
// Bỏ @Valid nếu không dùng DTO và validate phức tạp ở đây
// import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        log.info("Controller: Listing showtimes for movie ID: {}, Selected Date: {}", movieId, selectedDate);
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

    // --- 3. HIỂN THỊ FORM THÊM SUẤT CHIẾU CHO PHIM ---
    @GetMapping("/movie/{movieId}/add")
    public String showAddShowtimeForm(@PathVariable Long movieId, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.info("Controller: Showing add showtime form for movie ID: {}", movieId);
        try {
            Movie movie = movieService.getMovieById(movieId);
            Showtime showtime = new Showtime();
            showtime.setMovie(movie); // Gán sẵn phim
            showtime.setShowDate(LocalDate.now()); // Mặc định ngày hiện tại
            List<Room> activeRooms = roomService.getAllActiveRooms();
            
            model.addAttribute("showtime", showtime);
            model.addAttribute("movieTitle", movie.getTitle());
            model.addAttribute("allRooms", activeRooms); // Lấy danh sách phòng
            model.addAttribute("currentURI", request.getRequestURI());
            return "admin/showtime/form";
        } catch (ResourceNotFoundException e) {
            log.error("Controller: Movie not found (ID: {}) when showing add showtime form. {}", movieId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Movie not found. Cannot add showtime.");
            return "redirect:/admin/movies";
        }
    }

    // --- 4. HIỂN THỊ FORM SỬA SUẤT CHIẾU ---
    @GetMapping("/edit/{showtimeId}")
    public String showEditShowtimeForm(@PathVariable Long showtimeId, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.info("Controller: Showing edit showtime form for showtime ID: {}", showtimeId);
        try {
            Showtime showtime = showtimeService.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + showtimeId));
            List<Room> activeRooms = roomService.getAllActiveRooms();
            
            model.addAttribute("showtime", showtime);
            model.addAttribute("movieTitle", showtime.getMovie().getTitle());
            model.addAttribute("allRooms", activeRooms);
            model.addAttribute("currentURI", request.getRequestURI());
            // Truyền startTime dưới dạng String để input type="time" nhận đúng
            if (showtime.getStartTime() != null) {
                model.addAttribute("startTimeStringValue", showtime.getStartTime().toString());
            } else {
                model.addAttribute("startTimeStringValue", ""); // Hoặc giá trị mặc định
            }
            return "admin/showtime/form";
        } catch (ResourceNotFoundException e) {
            log.error("Controller: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/showtimes"; // Hoặc về trang list của phim nếu có movieId
        }
    }

    // --- 5. XỬ LÝ LƯU (THÊM/SỬA) SUẤT CHIẾU ---
    @PostMapping("/save")
    public String saveOrUpdateShowtime(@ModelAttribute("showtime") Showtime showtimeForm, // Bỏ @Valid tạm thời
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

        log.info("Controller: Attempting to save/update showtime. Movie ID: {}, Room ID: {}, Show Date: {}, Start Time Str: {}",
                 movieId, roomId, showDate, startTimeString);
        try {
            Movie movie = movieService.getMovieById(movieId); // Cần movie để gán
            showtimeForm.setMovie(movie);
        } catch (ResourceNotFoundException e){
             bindingResult.reject("movieId", "Movie with ID " + movieId + " not found.");
        }
        try {
            Room room = roomService.getRoomById(roomId); // Cần room để gán
            showtimeForm.setRoom(room);
        } catch (ResourceNotFoundException e) {
            bindingResult.reject("roomId", "Room with ID " + roomId + " not found.");
        }
        showtimeForm.setShowDate(showDate);
        showtimeForm.setExperience(experience);
        showtimeForm.setPrice(price);


        // Validate startTimeString thủ công
        if (startTimeString == null || startTimeString.isBlank()) {
            bindingResult.rejectValue("startTime", "showtime.startTime.notBlank", "Start time is required.");
        } else {
            try {
                LocalTime.parse(startTimeString); // Chỉ kiểm tra parse, service sẽ dùng lại
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
            model.addAttribute("startTimeStringValue", startTimeString); // Giữ lại giá trị người dùng nhập
            // model.addAttribute("showtime", showtimeForm); // @ModelAttribute tự thêm lại
            return "admin/showtime/form";
        }

        try {
            // Service sẽ xử lý việc tìm Movie, Room bằng ID và parse startTimeString
            if (showtimeForm.getId() == null) { // Tạo mới
                log.debug("Controller: Calling createShowtime service...");
                showtimeService.createShowtime(movieId, roomId, showDate, startTimeString, experience, price);
            } else { // Cập nhật
                log.debug("Controller: Calling updateShowtime service for ID: {}", showtimeForm.getId());
                showtimeService.updateShowtime(showtimeForm.getId(), movieId, roomId, showDate, startTimeString, experience, price);
            }
            return "redirect:/admin/showtimes/movie/" + movieId + (showDate != null ? "?date=" + showDate.toString() : "");

        } catch (ResourceNotFoundException | IllegalArgumentException e) { // Bắt lỗi cụ thể từ Service
            log.error("Controller: Error during showtime operation: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("movieTitle", showtimeForm.getMovie() != null ? showtimeForm.getMovie().getTitle() : "Unknown Movie");
            model.addAttribute("allRooms", roomService.getAllActiveRooms());
            model.addAttribute("currentURI", request.getRequestURI());
            model.addAttribute("startTimeStringValue", startTimeString);
            // model.addAttribute("showtime", showtimeForm); // @ModelAttribute tự thêm lại
            return "admin/showtime/form";
        }
    }

    // --- 6. XÓA SUẤT CHIẾU ---
    @PostMapping("/delete/{id}")
    public String deleteShowtime(@PathVariable Long id,
                               @RequestParam(value = "movieId", required = false) Long movieId,
                               @RequestParam(value = "returnDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
                               RedirectAttributes redirectAttributes) {
        // ... (Giữ nguyên logic xóa) ...
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