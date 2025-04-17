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
    public String userHomePage(Model model, Principal principal) { // Dùng Principal để lấy thông tin user
        log.info("Accessing user home page '/home'");

        String username = "Guest"; // Giá trị mặc định nếu không đăng nhập (dù request này nên được bảo vệ)
        if (principal != null) {
            username = principal.getName(); // Lấy username nếu đã đăng nhập
            log.info("User '{}' is logged in.", username);
            model.addAttribute("username", username);
        } else {
            // Trường hợp này không nên xảy ra nếu /home được bảo vệ đúng cách
            log.warn("Accessing /home without authentication!");
             // Có thể chuyển hướng về login hoặc trả lỗi, nhưng Security nên chặn trước đó
             // return "redirect:/login";
        }

        // Lấy danh sách phim
        List<Movie> movies = movieService.getAllMovies();
        log.info("Fetched {} movies for /home page.", movies.size());

        model.addAttribute("movies", movies);
        model.addAttribute("bannerUrl", "/images/banner.jpg");

        return "user/home";
    }

}