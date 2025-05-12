package com.example.demo.cinema.controller;
import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.service.MovieService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private MovieService movieService;

    @GetMapping
    public String showAdminDashboard(Model model, HttpServletRequest request) {
        log.info("Accessing Admin Dashboard");
        model.addAttribute("currentURI", request.getRequestURI());

        return "admin/dashboard";
    }

    @GetMapping("/movies")
    public String listMovies(Model model,
                             @PageableDefault(size = 10, sort = "title") Pageable pageable,
                             HttpServletRequest request) {
        log.info("Requesting movies for admin - Page: {}, Size: {}, Sort: {}",
                 pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<Movie> moviesPage = movieService.findMovies(pageable, null);

        model.addAttribute("moviesPage", moviesPage);
        model.addAttribute("currentPage", moviesPage.getNumber());
        model.addAttribute("pageSize", moviesPage.getSize());
        String sortParam = pageable.getSort().toString().replace(": ", ",");
        model.addAttribute("sortParam", sortParam);
        model.addAttribute("currentURI", request.getRequestURI());

        log.info("Returning movie list view - Page {}/{} - Size: {} - Total Elements: {}",
                 moviesPage.getNumber(), moviesPage.getTotalPages(), moviesPage.getSize(), moviesPage.getTotalElements());

        return "admin/movie/list";
    }

    @GetMapping("/movies/create")
    public String showCreateMovieForm(Model model, HttpServletRequest request) {
        log.info("Showing create movie form");
        model.addAttribute("movie", new Movie());
        model.addAttribute("currentURI", request.getRequestURI());
        return "admin/movie/form";
    }

    @GetMapping("/movies/edit/{id}")
    public String showEditMovieForm(@PathVariable Long id, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.info("Showing edit movie form for id: {}", id);
        try {
            Movie movie = movieService.getMovieById(id);

            model.addAttribute("movie", movie);
            model.addAttribute("currentURI", request.getRequestURI());
            return "admin/movie/form";
        } catch (Exception e) {
            log.error("Error finding movie with id {} for edit: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Movie not found."); // Thông báo lỗi flash
            return "redirect:/admin/movies";
        }
    }

    @PostMapping("/movies/save")
    public String saveOrUpdateMovie(@ModelAttribute("movie") Movie movieFromForm,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    Model model,
                                    HttpServletRequest request) {

        log.info("Controller: Received request to save/update movie: {}", movieFromForm.getTitle());
        log.debug("Controller: Received Genres String: {}", movieFromForm.getGenres());
        log.debug("Controller: Received Formats String: {}", movieFromForm.getAvailableFormats());
        
        if (bindingResult.hasErrors()) {
            log.error("Controller: Validation errors found: {}", bindingResult.getAllErrors());
            model.addAttribute("currentURI", request.getRequestURI());
            //model.addAttribute("movie", movieFromForm);
            return "admin/movie/form";
        }

        try {
            log.debug("Controller: Calling movieService.saveOrUpdate...");
            Movie savedOrUpdatedMovie = movieService.saveOrUpdate(movieFromForm);
            log.info("Controller: Service call completed successfully for Movie ID: {}", savedOrUpdatedMovie.getId());

            redirectAttributes.addFlashAttribute("successMessage", "Movie saved successfully!");
            return "redirect:/admin/movies";
        } catch (ResourceNotFoundException rnfe) {
            log.error("Controller: {}", rnfe.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", rnfe.getMessage());
            return "redirect:/admin/movies"; 
        } catch (Exception e) {
            log.error("Controller: Error during movie save/update: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error saving movie: " + e.getMessage());
            model.addAttribute("movie", movieFromForm); 
            model.addAttribute("currentURI", request.getRequestURI());
            return "admin/movie/form";
        }
    }


    @PostMapping("/movies/delete/{id}")
    public String deleteMovie(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.warn("Attempting to delete movie with id: {}", id);
        try {
            movieService.deleteMovie(id);
            log.info("Movie with id {} deleted", id);
            redirectAttributes.addFlashAttribute("successMessage", "Movie deleted successfully!");
        } catch (Exception e) {
             log.error("Error deleting movie with id {}: {}", id, e.getMessage(), e);
             redirectAttributes.addFlashAttribute("errorMessage", "Error deleting movie: " + e.getMessage());
        }
        return "redirect:/admin/movies";
    }

    @GetMapping("/movies/details/{id}")
    public String showMovieDetails(@PathVariable Long id, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        log.info("Showing movie details for admin, id: {}", id);
        try{
           Movie movie = movieService.getMovieById(id);

           model.addAttribute("movie", movie);
           model.addAttribute("currentURI", request.getRequestURI());
           model.addAttribute("genresString", request.getRequestURI());
           return "admin/movie/details";

        } catch (ResourceNotFoundException e) {
            log.error("Movie not found with id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Movie not found with id: " + id);
            return "redirect:/admin/movies";
        } catch (Exception e) {
            log.error("Error loading movie details for id {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading movie details.");
            return "redirect:/admin/movies";
        }
    }


}