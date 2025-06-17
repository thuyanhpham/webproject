package com.example.demo.cinema.controller;
import com.example.demo.cinema.dto.ReviewDTO;
import com.example.demo.cinema.dto.SeatPlanResponseDTO;
import com.example.demo.cinema.dto.UserReviewDTO;
import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.entity.Review;
import com.example.demo.cinema.entity.Showtime;
import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.service.MovieService;
import com.example.demo.cinema.service.ReviewService;
import com.example.demo.cinema.service.SeatPlanService;
import com.example.demo.cinema.service.ShowtimeService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/movies")
public class UserMovieController {

    private static final Logger log = LoggerFactory.getLogger(UserMovieController.class);

    private final MovieService movieService;
    private final ShowtimeService showtimeService;
    private final ReviewService reviewService; 
    private final SeatPlanService seatPlanService;

    private static final String CINEMA_NAME = "Boleto Cinema VN";
    private static final int REVIEWS_PER_PAGE = 5;

    @Autowired
    public UserMovieController(MovieService movieService, ShowtimeService showtimeService,ReviewService reviewService, SeatPlanService seatPlanService) {
        this.movieService = movieService;
        this.showtimeService = showtimeService;
        this.reviewService = reviewService;
        this.seatPlanService = seatPlanService;
    }

    @GetMapping
    public String listAllMoviesForUser(Model model,
                                       HttpServletRequest request,
                                       @RequestParam(name = "page", defaultValue = "1") int page,
                                       @RequestParam(name = "size", defaultValue = "9") int size,
                                       @RequestParam(name = "search", required = false) String searchTerm,
                                       @RequestParam(name = "sort", defaultValue = "title,asc") String sortParamValue) {

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
    }

    @GetMapping("/detail/{id}")
    public String showMovieDetails(@PathVariable Long id, Model model) {
            Movie movie = movieService.getMovieById(id);
            model.addAttribute("movie", movie);
            model.addAttribute("cinemaName", CINEMA_NAME);
            model.addAttribute("pageTitle", movie.getTitle() + " - Details");
            Pageable firstPageable = PageRequest.of(0, REVIEWS_PER_PAGE, Sort.by("timestamp").descending());
            Page<Review> reviewPage = reviewService.getReviewsForMovie(id, firstPageable);
            model.addAttribute("reviewsForDisplay", reviewPage.getContent());
            model.addAttribute("reviewPage", reviewPage);
            return "user/movie/moviedetail";
    }

    @PostMapping("/{movieId}/reviews/submit")
    public String submitReview(@PathVariable("movieId") Long movieId,
                               @RequestParam("rating") Integer rating,
                               @RequestParam(name = "title", required = false) String title,
                               @RequestParam("content") String content,
                               @AuthenticationPrincipal UserDetails currentUser,
                               RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("reviewErrorMessage", "You must be logged in to submit a review.");
            return "redirect:/login?redirect=/movies/detail/" + movieId;
        }
        String username = currentUser.getUsername();
        try {
            reviewService.createReview(username, movieId, rating, title, content);
            redirectAttributes.addFlashAttribute("reviewSuccessMessage", "Your review has been submitted successfully!");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("reviewErrorMessage", "You have already reviewed this movie.");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("reviewErrorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("reviewErrorMessage", "An error occurred while submitting your review. Please try again.");
        }
        return "redirect:/movies/detail/" + movieId + "#reviews-section";
    }

    @GetMapping("/api/{id}/reviews")
    @ResponseBody
    public Page<ReviewDTO> getMovieReviewsPageApi(
            @PathVariable("id") Long movieId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "" + REVIEWS_PER_PAGE, required = false) int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Review> reviewPageEntity = reviewService.getReviewsForMovie(movieId, pageable);
        Page<ReviewDTO> reviewDtoPage = reviewPageEntity.map(this::convertToReviewDTO);
        return reviewDtoPage;
    }

    @PostMapping("/api/reviews/{reviewId}/like")
    @ResponseBody
    public ResponseEntity<?> likeReview(@PathVariable Long reviewId, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "User not authenticated. Please login to like reviews."));
        }
        try {
            Review updatedReview = reviewService.handleLikeReview(reviewId, currentUser.getUsername());
            ReviewDTO updatedReviewDTO = convertToReviewDTO(updatedReview);
            return ResponseEntity.ok(updatedReviewDTO);
        } catch (ResourceNotFoundException e) {
            log.warn("API: Like failed. Review not found with ID: {}", reviewId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("API: Like failed for review ID: {}. Reason: {}", reviewId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("API: Error liking review with ID: {}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Could not process like action."));
        }
    }

    @PostMapping("/api/reviews/{reviewId}/dislike")
    @ResponseBody
    public ResponseEntity<?> dislikeReview(@PathVariable Long reviewId, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "User not authenticated. Please login to dislike reviews."));
        }
        try {
            Review updatedReview = reviewService.handleDislikeReview(reviewId, currentUser.getUsername());
            ReviewDTO updatedReviewDTO = convertToReviewDTO(updatedReview);
            return ResponseEntity.ok(updatedReviewDTO);
        } catch (ResourceNotFoundException e) {
            log.warn("API: Dislike failed. Review not found with ID: {}", reviewId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.warn("API: Dislike failed for review ID: {}. Reason: {}", reviewId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("API: Error disliking review with ID: {}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Could not process dislike action."));
        }
    }

    
    @PostMapping("/api/reviews/{reviewId}/report")
    @ResponseBody
    public ResponseEntity<?> reportAbuse(@PathVariable Long reviewId, @AuthenticationPrincipal UserDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "User not authenticated. Please login to report reviews."));
        }
        try {
            
            Map<String, Object> reportResult = reviewService.handleReportAbuse(reviewId, currentUser.getUsername());
            return ResponseEntity.ok(reportResult);

        } catch (ResourceNotFoundException e) {
            log.warn("API: Report Abuse failed. Resource not found. Review ID: {}, Error: {}", reviewId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) { 
            log.warn("API: Report Abuse failed for review ID: {}. Reason: {}", reviewId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("API: Error reporting abuse for review with ID: {}", reviewId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Could not process report abuse action."));
        }
    }

    private ReviewDTO convertToReviewDTO(Review reviewEntity) {
        if (reviewEntity == null) {
            return null;
        }
        ReviewDTO dto = new ReviewDTO();
        dto.setId(reviewEntity.getId());
        dto.setRating(reviewEntity.getRating());
        dto.setTitle(reviewEntity.getTitle());
        dto.setContent(reviewEntity.getContent());
        dto.setTimestamp(reviewEntity.getTimestamp());
        dto.setLikes(reviewEntity.getLikes());
        dto.setDislikes(reviewEntity.getDislikes());
        dto.setVerified(reviewEntity.getVerified());

        User userEntity = reviewEntity.getUser();
        if (userEntity != null) {
            UserReviewDTO userDto = new UserReviewDTO();
            userDto.setUsername(userEntity.getUsername());
            userDto.setFullname(userEntity.getFullname());
            userDto.setAvatarUrl(userEntity.getAvatarUrl());
            dto.setUser(userDto);
        }
        return dto;
    }

    @GetMapping("/{movieId}/showtimes")
    public String showMovieTicketPlan(
            @PathVariable("movieId") Long movieId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedDate,
            @RequestParam(value = "experience", required = false) String selectedExperience,
            Model model,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

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
    }

    @GetMapping("/showtimes/{showtimeId}/seat-plan")
    public String showSeatPlanPage(@PathVariable Long showtimeId, Model model) {
            SeatPlanResponseDTO seatPlanData = seatPlanService.getSeatPlanForShowtime(showtimeId);
            model.addAttribute("seatPlan", seatPlanData);
            return "user/movie/movie-seat-plan";
    }
}
