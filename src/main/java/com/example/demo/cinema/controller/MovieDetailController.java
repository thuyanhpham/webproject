package com.example.demo.cinema.controller;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.entity.Showtime;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.service.MovieService;
import com.example.demo.cinema.service.ShowtimeService;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    private static final String CINEMA_NAME = "Boleto Cinema VN";
    private static final String CINEMA_CITY = "Ha Noi City";

    @Autowired
    public MovieDetailController(MovieService movieService, ShowtimeService showtimeService) {
        this.movieService = movieService;
        this.showtimeService = showtimeService;
    }
    @GetMapping
    public String listAllMoviesForUser(Model model,
                                       HttpServletRequest request,
                                       @RequestParam(name = "page", defaultValue = "1") int page,
                                       @RequestParam(name = "size", defaultValue = "9") int size,
                                       @RequestParam(name = "search", required = false) String searchTerm,
                                       @RequestParam(name = "sort", defaultValue = "title,asc") String sortParamValue) { 
        log.info("Request for user movie list. Page: {}, Size: {}, Search: '{}', Sort: {}", page, size, searchTerm, sortParamValue);
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
                log.debug("Searching movies with term: {}", searchTerm.trim());
                moviesPageResult = movieService.searchMovies(searchTerm.trim(), pageable);
                model.addAttribute("searchTerm", searchTerm.trim());
            } else {
                log.debug("Fetching all movies paginated.");
                moviesPageResult = movieService.getAllMoviesPaginated(pageable);
                model.addAttribute("searchTerm", "");
            }
            
            if (moviesPageResult != null) {
                log.info("Controller: moviesPage content: Is empty? {}, Total elements: {}, Total pages: {}",
                        moviesPageResult.isEmpty(), moviesPageResult.getTotalElements(), moviesPageResult.getTotalPages());
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
        log.info("Request received for movie details with ID: {}", id);
        try {
            Movie movie = movieService.getMovieById(id);
            model.addAttribute("movie", movie);
            model.addAttribute("cinemaName", CINEMA_NAME);
            model.addAttribute("cinemaCity", CINEMA_CITY);

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

    @GetMapping("/{movieId}/showtimes")
    public String showMovieTicketPlan(
            @PathVariable("movieId") Long movieId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate selectedDate,
            @RequestParam(value = "experience", required = false) String selectedExperience,
            Model model,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        log.info("Request received for showtimes - Movie ID: {}, Date: {}, Experience: {}", movieId, selectedDate, selectedExperience);

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


            log.info("Found {} showtimes for movie '{}' at {} on {}. Rendering ticket plan page.",
                     showtimes.size(), movie.getTitle(), dateToFilter);

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