package com.example.demo.cinema.controller;
import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.service.MovieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
public class MovieController {

    private final MovieService movieService;

    @Autowired
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/movielist")
    public String showMovieGrid(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "12") int size,
                                @RequestParam(required = false) String query,
                                @RequestParam(required = false, defaultValue = "title,asc") String sort,
                                HttpServletRequest request ) {
        Sort sortObj = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            try {
                String[] sortParams = sort.split(",");
                if (sortParams.length == 2) {
                    Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
                    sortObj = Sort.by(direction, sortParams[0]);
                } else if (sortParams.length == 1) {
                    sortObj = Sort.by(Sort.Direction.ASC, sortParams[0]);
                }
            } catch (Exception e) {
            }
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);
    
        Page<Movie> moviePage = movieService.findMovies(pageable, query );

        model.addAttribute("moviesPage", moviePage);
        model.addAttribute("movies", moviePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("sortParam", sort);
        model.addAttribute("queryParam", query);
        model.addAttribute("requestURI", request.getRequestURI());
        return "user/movie/movielist"; 
    }
}
