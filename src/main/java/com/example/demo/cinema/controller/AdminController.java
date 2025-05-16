package com.example.demo.cinema.controller;
import com.example.demo.cinema.entity.Format;
import com.example.demo.cinema.entity.Genre;
import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.service.FormatService;
import com.example.demo.cinema.service.GenreService;
import com.example.demo.cinema.service.MovieService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    
    @Autowired
    private GenreService genreService;
    
    @Autowired
    private FormatService formatService;

    @GetMapping
    public String showAdminDashboard(Model model, HttpServletRequest request) {
        model.addAttribute("currentURI", request.getRequestURI());
        return "admin/dashboard";
    }

    @GetMapping("/movies")
    public String listMovies(Model model,
                             @PageableDefault(size = 10, sort = "title") Pageable pageable,
                             HttpServletRequest request) {
        Page<Movie> moviesPage = movieService.findMovies(pageable, null);

        model.addAttribute("moviesPage", moviesPage);
        model.addAttribute("currentPage", moviesPage.getNumber());
        model.addAttribute("pageSize", moviesPage.getSize());
        String sortParam = pageable.getSort().toString().replace(": ", ",");
        model.addAttribute("sortParam", sortParam);
        model.addAttribute("currentURI", request.getRequestURI());

        return "admin/movie/list";
    }

    @GetMapping("/movies/add")
    public String showCreateMovieForm(Model model, HttpServletRequest request) {
        model.addAttribute("movie", new Movie());
        model.addAttribute("pageTitle", "Add New Movie");
        model.addAttribute("currentURI", request.getRequestURI());
        model.addAttribute("allGenres", genreService.getAllGenres());
        model.addAttribute("selectedGenres", List.of());
        model.addAttribute("allFormats", formatService.getAllFormats());
        return "admin/movie/form";
    }

    @GetMapping("/movies/edit/{id}")
    public String showEditMovieForm(@PathVariable Long id, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            Movie movie = movieService.getMovieById(id);

            model.addAttribute("movie", movie);
            model.addAttribute("pageTitle", "Edit Movie: " + movie.getTitle());
            model.addAttribute("currentURI", request.getRequestURI());
            model.addAttribute("allGenres", genreService.getAllGenres());
            model.addAttribute("allFormats", formatService.getAllFormats());
            
            if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
                model.addAttribute("selectedGenres",
                        movie.getGenres().stream().map(Genre::getName).collect(Collectors.toList()));
            } else {
                model.addAttribute("selectedGenres", List.of());
            }

            return "admin/movie/form";
        } catch (Exception e) {
            log.error("Error finding movie with id {} for edit: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Movie not found."); 
            return "redirect:/admin/movies";
        }
    }

    @PostMapping("/movies/save")
    public String saveOrUpdateMovie(@ModelAttribute("movie") Movie movieFromForm,
                                    BindingResult bindingResult,
                                    @RequestParam(name = "selectedGenresArray", required = false) List<String> selectedGenreNames,
                                    @RequestParam(name = "formatId", required = false) Long formatId,
                                    RedirectAttributes redirectAttributes,
                                    Model model,
                                    HttpServletRequest request) {
    	if (selectedGenreNames != null && !selectedGenreNames.isEmpty()) {
            Set<Genre> resolvedGenres = genreService.findOrCreateGenresByNames(selectedGenreNames);
            movieFromForm.setGenres(resolvedGenres);
        } else {
            movieFromForm.setGenres(new HashSet<>());
        }

    	if (formatId != null && formatId > 0) {
            Optional<Format> formatOptional = formatService.findById(formatId);
            if (formatOptional.isPresent()) {
                movieFromForm.setFormat(formatOptional.get());
            } else {
                movieFromForm.setFormat(null);
            }
        }
    	
        if (bindingResult.hasErrors()) {
            log.error("Controller: Validation errors found: {}", bindingResult.getAllErrors());
            model.addAttribute("currentURI", request.getRequestURI());
            model.addAttribute("movie", movieFromForm);
            model.addAttribute("pageTitle", movieFromForm.getId() == null ? "Add New Movie" : "Edit Movie: " + movieFromForm.getTitle());
            model.addAttribute("allGenres", genreService.getAllGenres());
            model.addAttribute("selectedGenres", selectedGenreNames != null ? selectedGenreNames : List.of());
            model.addAttribute("allFormats", formatService.getAllFormats());
            return "admin/movie/form";
        }

        try {
        	if (selectedGenreNames != null && !selectedGenreNames.isEmpty()) {
                Set<Genre> resolvedGenres = genreService.findOrCreateGenresByNames(selectedGenreNames);
                movieFromForm.setGenres(resolvedGenres);
            } else {
                movieFromForm.setGenres(new HashSet<>());
            }
            Movie savedOrUpdatedMovie = movieService.saveOrUpdate(movieFromForm);
            return "redirect:/admin/movies";
        } catch (ResourceNotFoundException rnfe) {
            redirectAttributes.addFlashAttribute("errorMessage", rnfe.getMessage());
            return "redirect:/admin/movies"; 
        } catch (Exception e) {
            log.error("Controller: Error during movie save/update: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error saving movie: " + e.getMessage());
            model.addAttribute("movie", movieFromForm); 
            model.addAttribute("pageTitle", movieFromForm.getId() == null ? "Add New Movie" : "Edit Movie: " + movieFromForm.getTitle());
            model.addAttribute("allGenres", genreService.getAllGenres());
            model.addAttribute("selectedGenres", selectedGenreNames != null ? selectedGenreNames : List.of());
            model.addAttribute("allFormats", formatService.getAllFormats());
            model.addAttribute("currentURI", request.getRequestURI());
            return "admin/movie/form";
        }
    }


    @PostMapping("/movies/delete/{id}")
    public String deleteMovie(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.warn("Attempting to delete movie with id: {}", id);
        try {
            movieService.deleteMovie(id);
        } catch (Exception e) {
             log.error("Error deleting movie with id {}: {}", id, e.getMessage(), e);
             redirectAttributes.addFlashAttribute("errorMessage", "Error deleting movie: " + e.getMessage());
        }
        return "redirect:/admin/movies";
    }

    @GetMapping("/movies/details/{id}")
    public String showMovieDetails(@PathVariable Long id, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try{
           Movie movie = movieService.getMovieById(id);

           model.addAttribute("movie", movie);
           model.addAttribute("currentURI", request.getRequestURI());
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