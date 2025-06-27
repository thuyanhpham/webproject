package com.example.demo.cinema.controller;
import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.entity.MovieStatus;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.service.FormatService;
import com.example.demo.cinema.service.GenreService;
import com.example.demo.cinema.service.MovieService;
import com.example.demo.cinema.service.PersonService;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/movies")
public class AdminController {

    @Autowired
    private MovieService movieService;
    
    @Autowired
    private GenreService genreService;
    
    @Autowired
    private FormatService formatService;

    @Autowired
    private PersonService personService;

    @GetMapping
    public String listMovies(Model model, @PageableDefault(size = 10, sort = "title") Pageable pageable, @RequestParam(required = false) String searchTerm) {
        Page<Movie> moviesPage = movieService.findMovies(pageable, searchTerm);
        model.addAttribute("moviesPage", moviesPage);
        return "admin/movie/list";
    }

    private void addCommonAttributesToModel(Model model) {
        model.addAttribute("allGenres", genreService.getAllGenres());
        model.addAttribute("allFormats", formatService.getAllFormats());
        model.addAttribute("allMovieStatuses", MovieStatus.values());
        model.addAttribute("allPeople", personService.findAll());
    }

    @GetMapping("/new")
    public String showNewMovieForm(Model model) {
        model.addAttribute("movie", new Movie());
        model.addAttribute("pageTitle", "Add New Movie");
        addCommonAttributesToModel(model);
        return "admin/movie/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditMovieForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Movie movie = movieService.getMovieById(id);
            model.addAttribute("movie", movie);
            model.addAttribute("pageTitle", "Edit Movie: " + movie.getTitle());
            addCommonAttributesToModel(model);
            return "admin/movie/form";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Movie not found.");
            return "redirect:/admin/movies";
        }
    }

    @PostMapping("/save")
    public String saveMovie(@ModelAttribute("movie") Movie movie,
                            BindingResult bindingResult,
                            @RequestParam("posterFile") MultipartFile posterFile,
                            @RequestParam(name = "directorIds", required = false) List<Long> directorIds,
                            @RequestParam(name = "actorIds", required = false) List<Long> actorIds,
                            RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", movie.getId() == null ? "Add New Movie" : "Edit Movie");
            addCommonAttributesToModel(model);
            return "admin/movie/form";
        }

        try {
            movieService.saveMovieAndCrew(movie, posterFile, directorIds, actorIds);
            
            redirectAttributes.addFlashAttribute("successMessage", "Movie and its crew have been saved successfully.");
            return "redirect:/admin/movies";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error saving movie: " + e.getMessage());
            model.addAttribute("pageTitle", movie.getId() == null ? "Add New Movie" : "Edit Movie");
            addCommonAttributesToModel(model);
            return "admin/movie/form";
        }
    }

    @PostMapping("/delete/{id}")
        public String deleteMovie(@PathVariable Long id) {
                movieService.deleteMovie(id);
            return "redirect:/admin/movies";
        }

    @GetMapping("/details/{id}")
    public String showMovieDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Movie movie = movieService.getMovieById(id);
            model.addAttribute("movie", movie);
            return "admin/movie/details";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/movies";
        }
    }
}