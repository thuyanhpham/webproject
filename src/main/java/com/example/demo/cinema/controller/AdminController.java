package com.example.demo.cinema.controller;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.repository.GenreRepository;
import com.example.demo.cinema.service.MovieService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private MovieService movieService;

    @Autowired
    private GenreRepository genreRepository;

    @GetMapping
    public String showAdminDashboard(Model model) {
        log.info("Accessing Admin Dashboard");
        // TODO: Thêm logic để lấy dữ liệu cần thiết cho dashboard (ví dụ: số lượng
        // phim, user...)
        // model.addAttribute("totalMovies", movieService.countMovies());
        // model.addAttribute("totalUsers", userService.countUsers()); // Giả sử có
        // userService
        return "admin/dashboard";
    }

    @GetMapping("/movies")
    public String listMovies(Model model) {
        log.info("Listing movies for admin");
        model.addAttribute("movies", movieService.getAllMovies());
        return "admin/movie/list"; // View: templates/admin/movie/list.html
    }

    @GetMapping("/movies/create")
    public String showCreateMovieForm(Model model) {
        log.info("Showing create movie form");
        model.addAttribute("movie", new Movie());
        model.addAttribute("allGenres", genreRepository.findAll());
        return "admin/movie/form"; // View: templates/admin/movie/form.html
    }

    @PostMapping("/movies/save")
    public String saveOrUpdateMovie(@ModelAttribute("movie") Movie movie) {
        log.info("Saving/Updating movie: {}", movie.getTitle());
        movieService.saveMovie(movie);
        return "redirect:/admin/movies";
    }

    @GetMapping("/movies/edit/{id}")
    public String showEditMovieForm(@PathVariable Long id, Model model) {
        log.info("Showing edit movie form for id: {}", id);
        Movie movie = movieService.getMovieById(id);
        if (movie == null) {
            log.warn("Movie with id {} not found for editing", id);
            return "redirect:/admin/movies?error=Movie_not_found";
        }
        model.addAttribute("movie", movie);
        model.addAttribute("allGenres", genreRepository.findAll());
        return "admin/movie/form";
    }

    // Lưu ý: Nên đổi thành POST/DELETE
    @GetMapping("/movies/delete/{id}")
    public String deleteMovie(@PathVariable Long id) {
        log.warn("Attempting to delete movie with id: {}", id); // Dùng warn vì đây là hành động xóa
        movieService.deleteMovie(id);
        log.info("Movie with id {} deleted", id);
        return "redirect:/admin/movies";
    }

    @GetMapping("/movies/details/{id}")
    public String showMovieDetails(@PathVariable Long id, Model model) {
        log.info("Showing movie details for admin, id: {}", id);
        Movie movie = movieService.getMovieById(id);
        if (movie == null) {
            log.warn("Movie with id {} not found for details view", id);
            return "redirect:/admin/movies?error=Movie_not_found";
        }
        model.addAttribute("movie", movie);
        return "admin/movie/details"; // View: templates/admin/movie/details.html
    }

}