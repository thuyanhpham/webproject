package com.example.demo.cinema.controller;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.service.MovieService;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private MovieService movieService;


    @GetMapping("/home")
    public String userHomePage(Model model, Principal principal) {
        String username = "Guest";
        if (principal != null) {
            username = principal.getName();
            model.addAttribute("username", username);
        } else {
            log.warn("Accessing /home without authentication!");
        }

        List<Movie> nowShowingMovies = movieService.findNowShowingMovies();
        model.addAttribute("nowShowingMovies", nowShowingMovies);
        List<Movie> comingSoonMovies = movieService.findComingSoonMovies(); 
        model.addAttribute("comingSoonMovies", comingSoonMovies);
        model.addAttribute("bannerUrl", "/images/banner.jpg"); 
        return "user/home";
    }
}