package com.example.demo.cinema.controller;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.entity.Review;
import com.example.demo.cinema.entity.Showtime;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.repository.ReviewRepository;
import com.example.demo.cinema.service.MovieService;
import com.example.demo.cinema.service.ReviewService;
import com.example.demo.cinema.service.ShowtimeService;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/movies")
public class MovieDetailController {

    private static final Logger log = LoggerFactory.getLogger(MovieDetailController.class);

    private final MovieService movieService;
    private final ShowtimeService showtimeService;
    private final ReviewService reviewService;

    private static final String CINEMA_NAME = "Boleto Cinema VN";
    private static final String CINEMA_CITY = "Ha Noi City";
    private static final int REVIEWS_PER_PAGE = 5;
    
    @Autowired
    public MovieDetailController(MovieService movieService, ShowtimeService showtimeService,ReviewService reviewService) {
        this.movieService = movieService;
        this.showtimeService = showtimeService;
        this.reviewService = reviewService; 
    }
    @GetMapping
    public String listAllMoviesForUser(Model model,
                                       HttpServletRequest request,
                                       @RequestParam(name = "page", defaultValue = "1") int page,
                                       @RequestParam(name = "size", defaultValue = "9") int size,
                                       @RequestParam(name = "search", required = false) String searchTerm,
                                       @RequestParam(name = "sort", defaultValue = "title,asc") String sortParamValue) { 
        
        try {
            String[] sortParamsArray = sortParamValue.split(",");
            String sortField = sortParamsArray[0];
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortParamsArray.length > 1 && sortParamsArray[1].equalsIgnoreCase("desc")) {
                direction = Sort.Direction.DESC;
            }
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortField));

            Page<Movie> moviesPageResult;

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
             
                moviesPageResult = movieService.searchMovies(searchTerm.trim(), pageable);
                model.addAttribute("searchTerm", searchTerm.trim());
            } else {
           
                moviesPageResult = movieService.getAllMoviesPaginated(pageable);
                model.addAttribute("searchTerm", "");
            }
            
            if (moviesPageResult != null) {
              
                if (!moviesPageResult.isEmpty()) {
                    moviesPageResult.getContent().forEach(movie ->
                            log.info("Controller: Fetched movie from service: ID={}, Title={}, Status={}",
                                    movie.getId(), movie.getTitle(), movie.getStatus() != null ? movie.getStatus().name() : "N/A") // Giả sử Movie có getStatus() là Enum
                    );
                }
            } else {
                log.warn("Controller: moviesPage received from service is NULL!");
            }
            
            model.addAttribute("moviesPage", moviesPageResult);
            model.addAttribute("pageTitle", "All Movies");
            model.addAttribute("sortParam", sortParamValue); 
            model.addAttribute("pageSize", size);
            model.addAttribute("currentRequestURI", request.getRequestURI());

            if (moviesPageResult != null && moviesPageResult.getTotalPages() > 0) {
                int totalPages = moviesPageResult.getTotalPages();
                List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                        .boxed()
                        .collect(Collectors.toList());
                model.addAttribute("pageNumbers", pageNumbers);
            } else {
                 model.addAttribute("pageNumbers", List.of());
            }

            return "user/movie/movielist";

        } catch (Exception e) {
            log.error("An unexpected error occurred while fetching movie list for user. Details: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "An unexpected error occurred while retrieving movies. Please try again later.");
            model.addAttribute("currentRequestURI", request.getRequestURI());
            return "error/500";
        }
    }

    @GetMapping("/detail/{id}")
    public String showMovieDetails(@PathVariable Long id, Model model) {
       
        try {
            Movie movie = movieService.getMovieById(id);
            model.addAttribute("movie", movie);
            model.addAttribute("cinemaName", CINEMA_NAME);
            model.addAttribute("cinemaCity", CINEMA_CITY);
            model.addAttribute("pageTitle", movie.getTitle() + " - Details"); 
   
            Pageable firstPageable = PageRequest.of(0, REVIEWS_PER_PAGE, Sort.by("timestamp").descending());
            Page<Review> reviewPage = reviewService.getReviewsForMovie(id, firstPageable);
            
            model.addAttribute("reviewsForDisplay", reviewPage.getContent());
            model.addAttribute("reviewPage", reviewPage);
            
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
    
    // --- PHƯƠNG THỨC XỬ LÝ SUBMIT REVIEW MỚI ---
    @PostMapping("/{movieId}/reviews/submit")
    public String submitReview(@PathVariable("movieId") Long movieId,
                               @RequestParam("rating") Integer rating,
                               @RequestParam(name = "title", required = false) String title,
                               @RequestParam("content") String content,
                               @AuthenticationPrincipal UserDetails currentUser, // Lấy user đang đăng nhập
                               RedirectAttributes redirectAttributes) {

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("reviewErrorMessage", "You must be logged in to submit a review.");
            log.warn("Attempt to submit review by unauthenticated user for movie ID: {}", movieId);
            return "redirect:/login?redirect=/movies/detail/" + movieId; // Chuyển đến trang login
        }

        String username = currentUser.getUsername();
        log.debug("User {} attempting to submit review for movie ID: {}", username, movieId);

        try {
            // Gọi ReviewService để tạo và lưu review
            reviewService.createReview(username, movieId, rating, title, content);
            redirectAttributes.addFlashAttribute("reviewSuccessMessage", "Your review has been submitted successfully!");
            log.info("Review submitted successfully by user {} for movie ID {}", username, movieId);
        } catch (DataIntegrityViolationException e) { // Bắt lỗi nếu user đã review (nếu có unique constraint)
            log.warn("User {} already reviewed movie ID {}: {}", username, movieId, e.getMessage());
            redirectAttributes.addFlashAttribute("reviewErrorMessage", "You have already reviewed this movie.");
        } catch (ResourceNotFoundException e) {
            log.error("Error submitting review: Resource not found - {}", e.getMessage());
            redirectAttributes.addFlashAttribute("reviewErrorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("General error submitting review for movie ID {} by user {}: {}", movieId, username, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("reviewErrorMessage", "An error occurred while submitting your review. Please try again.");
        }
        // Thêm #reviews-section để trình duyệt tự động cuộn đến phần review sau khi redirect
        return "redirect:/movies/detail/" + movieId + "#reviews-section";
    }
    
 // --- API ENDPOINT CHO "LOAD MORE" REVIEWS (SẼ DÙNG VỚI JAVASCRIPT) ---
    @GetMapping("/api/{id}/reviews") // Phân biệt với URL trang HTML bằng "/api/"
    @ResponseBody // QUAN TRỌNG: Báo cho Spring biết trả về dữ liệu (JSON), không phải tên template
    public Page<Review> getMovieReviewsPageApi(
            @PathVariable("id") Long movieId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            // Lấy size từ REVIEWS_PER_PAGE hoặc cho client tự định nghĩa
            @RequestParam(name = "size", defaultValue = "" + REVIEWS_PER_PAGE, required = false) int size) {

        log.debug("API: Requesting reviews for movie ID: {}, page: {}, size: {}", movieId, page, size);
        // Sắp xếp theo timestamp giảm dần (mới nhất trước)
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Review> reviewPageData = reviewService.getReviewsForMovie(movieId, pageable); // Gọi ReviewService
        log.debug("API: Found {} reviews for movie ID: {} on page: {}. Total pages: {}",
                reviewPageData.getNumberOfElements(), movieId, page, reviewPageData.getTotalPages());
        return reviewPageData; // Spring Boot (với Jackson) sẽ tự động chuyển Page<Review> thành JSON
    }

    @GetMapping("/{movieId}/showtimes")
    public String showMovieTicketPlan(
            @PathVariable("movieId") Long movieId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedDate,
            @RequestParam(value = "experience", required = false) String selectedExperience,
            Model model,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

 

        try {
            Movie movie = movieService.getMovieById(movieId);
            LocalDate dateToFilter = (selectedDate != null) ? selectedDate : LocalDate.now();
            List<LocalDate> availableDates = showtimeService.findAvailableDatesForMovie(movieId);
            List<String> availableExperiences = showtimeService.findAvailableExperiencesForMovieAndDate(movieId, dateToFilter);
            List<Showtime> showtimes = showtimeService.findAvailableShowtimes(movieId, dateToFilter, selectedExperience);

            model.addAttribute("movie", movie);
            model.addAttribute("selectedDate", dateToFilter);
            model.addAttribute("selectedExperience", selectedExperience);
            model.addAttribute("availableDates", availableDates);
            model.addAttribute("availableExperiences", availableExperiences);
            model.addAttribute("showtimes", showtimes);
            model.addAttribute("cinemaName", CINEMA_NAME);
            model.addAttribute("pageTitle", movie.getTitle() + " - Showtimes");


           

            return "user/movie/movieticketplan";

        } catch (ResourceNotFoundException e) {
            log.error("Resource not found while getting showtimes for Movie ID: {}", movieId, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/movies/detail/" + movieId;
        } catch (Exception e) {
            log.error("An unexpected error occurred while getting showtimes for Movie ID: {}", movieId, e);
            model.addAttribute("errorMessage", "An unexpected error occurred while retrieving showtime information.");
            return "error/500";
        }
    }
}