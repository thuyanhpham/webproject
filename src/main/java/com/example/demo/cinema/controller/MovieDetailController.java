package com.example.demo.cinema.controller;

// Import các class cần thiết...
import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.entity.Showtime;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.service.MovieService;
import com.example.demo.cinema.service.ShowtimeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class MovieDetailController {

    private static final Logger log = LoggerFactory.getLogger(MovieDetailController.class);

    private final MovieService movieService;
    private final ShowtimeService showtimeService;

   
    private static final String CINEMA_NAME = "Boleto Cinema VN";
    private static final String CINEMA_CITY = "Ha Noi City";
    //private static final String CINEMA_ADDRESS = "123 Nguyễn Huệ, Quận 1, TP.HCM";
 

    @Autowired
    public MovieDetailController(MovieService movieService, ShowtimeService showtimeService) {
        this.movieService = movieService;
        this.showtimeService = showtimeService;
    }

    /**
     * Hiển thị trang chi tiết phim (moviedetail.html).
     */
    @GetMapping("/movie/{id}")
    public String showMovieDetails(@PathVariable Long id, Model model) {
        // Giữ nguyên logic xử lý chi tiết phim
        log.info("Request received for movie details with ID: {}", id);
        try {
            Movie movie = movieService.getMovieById(id);
            model.addAttribute("movie", movie);

             // Thêm thông tin rạp vào trang chi tiết phim nếu cần
             model.addAttribute("cinemaName", CINEMA_NAME);
             model.addAttribute("cinemaCity", CINEMA_CITY);
             //model.addAttribute("cinemaAddress", CINEMA_ADDRESS);

            log.info("Movie found: {}. Rendering detail page.", movie.getTitle());
            return "user/movie/moviedetail";
        } catch (ResourceNotFoundException e) {
            log.error("Movie not found for ID: {}", id, e);
            model.addAttribute("errorMessage", "Movie not found with ID: " + id);
            return "error/404";
        } catch (Exception e) {
            log.error("An unexpected error occurred while fetching movie details for ID: {}", id, e);
            model.addAttribute("errorMessage", "An unexpected error occurred.");
            return "error/500";
        }
    }

    /**
     * Hiển thị trang chọn lịch chiếu (movie-ticket-plan.html) cho một phim cụ thể.
     * 
     */
    @GetMapping("/movies/{movieId}/showtimes")
    public String showMovieTicketPlan(
            @PathVariable("movieId") Long movieId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedDate,
            @RequestParam(value = "experience", required = false) String selectedExperience,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.info("Request received for showtimes - Movie ID: {}, Date: {}, Experience: {}", movieId, selectedDate, selectedExperience);

        try {
            // 1. Lấy thông tin chi tiết phim
            Movie movie = movieService.getMovieById(movieId);

            // 2. Xác định ngày lọc
            LocalDate dateToFilter = (selectedDate != null) ? selectedDate : LocalDate.now();

            // 3. Lấy danh sách ngày có suất chiếu
            List<LocalDate> availableDates = showtimeService.findAvailableDatesForMovie(movieId);

            // 4. Lấy danh sách định dạng có sẵn vào ngày đã chọn
            List<String> availableExperiences = showtimeService.findAvailableExperiencesForMovieAndDate(movieId, dateToFilter);

            // 5. Lấy danh sách suất chiếu phù hợp
            // Lưu ý: ShowtimeService.findAvailableShowtimes KHÔNG cần cinemaId theo thiết kế hiện tại
            List<Showtime> showtimes = showtimeService.findAvailableShowtimes(movieId, dateToFilter, selectedExperience);

            // 6. Đưa dữ liệu vào Model
            model.addAttribute("movie", movie);
            model.addAttribute("selectedDate", dateToFilter);
            model.addAttribute("selectedExperience", selectedExperience);
            model.addAttribute("availableDates", availableDates);
            model.addAttribute("availableExperiences", availableExperiences);
            model.addAttribute("showtimes", showtimes);

            // --- Thêm thông tin rạp phim trực tiếp vào Model ---
            model.addAttribute("cinemaName", CINEMA_NAME);
            //model.addAttribute("cinemaCity", CINEMA_CITY);
            //model.addAttribute("cinemaAddress", CINEMA_ADDRESS);
            // --------------------------------------------------

            log.info("Found {} showtimes for movie '{}' at {} on {}. Rendering ticket plan page.",
                     showtimes.size(), movie.getTitle(), dateToFilter);

            // 7. Trả về tên template
            return "user/movie/movieticketplan";

        } catch (ResourceNotFoundException e) {
            log.error("Resource not found while getting showtimes for Movie ID: {}", movieId, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/movies"; 
        } catch (Exception e) {
            log.error("An unexpected error occurred while getting showtimes for Movie ID: {}", movieId, e);
            model.addAttribute("errorMessage", "An unexpected error occurred while retrieving showtime information.");
            return "error/500";
        }
    }

    
}
