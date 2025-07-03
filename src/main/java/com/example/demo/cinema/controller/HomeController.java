package com.example.demo.cinema.controller;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.service.MovieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private MovieService movieService;

    @GetMapping("/")
    public String handleLoginRedirect(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_ADMIN")); 

            if (isAdmin) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/home";
            }
        }
        return "redirect:/home"; 
    }

    @GetMapping("/home")
    public String userHomePage(Model model, Principal principal, HttpServletRequest request) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        List<Movie> nowShowingMovies = movieService.findNowShowingMovies();
        model.addAttribute("nowShowingMovies", nowShowingMovies);
        List<Movie> comingSoonMovies = movieService.findComingSoonMovies(); 
        model.addAttribute("comingSoonMovies", comingSoonMovies);      
        return "user/home";
    }

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model, HttpServletRequest request) {
        model.addAttribute("currentURI", request.getRequestURI());
        return "admin/dashboard"; 
    }
}