package com.example.demo.cinema.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.service.MovieService;

@Controller
public class HomeController {

	@Autowired
	private MovieService movieService;
	
	
	@GetMapping("/home")
	public String index(Model model) {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		
		List<Movie> movies = movieService.getAllMovies();
		
		model.addAttribute("username", username);
		model.addAttribute("movies", movies);
		model.addAttribute("bannerUrl", "/images/banner.jpg");
		
		return "index";
	}
	
}
