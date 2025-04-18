package com.example.demo.cinema.controller;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.service.MovieService;
import com.example.demo.cinema.service.ShowtimeService; // *** Cần import ShowtimeService ***

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    // *** Sử dụng constructor injection cho cả 2 service ***
    private final MovieService movieService;
    private final ShowtimeService showtimeService;

    @Autowired
    public HomeController(MovieService movieService, ShowtimeService showtimeService) {
        this.movieService = movieService;
        this.showtimeService = showtimeService;
    }

    // *** Map cả "/" và "/home" ***
    @GetMapping({ "/home" })
    public String userHomePage(Model model, Principal principal) {
        log.info("Accessing user home page '/' or '/home'");

        // *** Xử lý Principal (giữ nguyên) ***
        if (principal != null) {
            String username = principal.getName();
            log.info("User '{}' is logged in.", username);
            model.addAttribute("username", username);
        } else {
            log.info("User is accessing anonymously.");
        }

        try {
            // *** 1. Lấy danh sách phim đã phân loại ***
            List<Movie> nowShowing = movieService.findNowShowingMovies(); // Gọi phương thức đúng
            List<Movie> comingSoon = movieService.findComingSoonMovies(); // Gọi phương thức đúng

            // *** Thêm vào Model với tên đúng mà template cần ***
            model.addAttribute("nowShowingMovies", nowShowing != null ? nowShowing : Collections.emptyList());
            model.addAttribute("comingSoonMovies", comingSoon != null ? comingSoon : Collections.emptyList());
            log.info("Fetched movies: Now Showing ({}), Coming Soon ({})",
                    nowShowing != null ? nowShowing.size() : 0,
                    comingSoon != null ? comingSoon.size() : 0);

            // *** 2. Lấy danh sách ngày cho dropdown ***
            // *** Đảm bảo phương thức này tồn tại trong ShowtimeService ***
            List<LocalDate> availableDates = showtimeService.findAllAvailableShowtimeDates();
            model.addAttribute("availableDates", availableDates != null ? availableDates : Collections.emptyList());
            log.info("Fetched search dropdown data: Dates ({})",
                    availableDates != null ? availableDates.size() : 0);

            // *** 3. Thêm các thông tin khác (Banner) ***
            model.addAttribute("bannerUrl", "/images/banner01.jpg");

        } catch (Exception e) {
            // *** Xử lý lỗi cơ bản (giữ nguyên) ***
            log.error("Error fetching data for home page", e);
            model.addAttribute("pageError", "Could not load all page data. Please try again later.");
            model.addAttribute("nowShowingMovies", Collections.emptyList());
            model.addAttribute("comingSoonMovies", Collections.emptyList());
            model.addAttribute("availableDates", Collections.emptyList());
        }

        // *** Trả về tên template "home" ***
        return "home";
    }
}