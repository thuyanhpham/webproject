package com.example.demo.cinema.controller;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.service.MovieService;
// Import HttpServletRequest (dùng jakarta cho Spring Boot 3+, javax cho Spring Boot 2)
import jakarta.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletRequest; // Nếu dùng Spring Boot 2
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
                                HttpServletRequest request // <<<--- 1. Inject HttpServletRequest
                               ) {

        // --- Phần xử lý Sort và Pageable giữ nguyên ---
        Sort sortObj = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            try {
                String[] sortParams = sort.split(",");
                if (sortParams.length == 2) {
                    Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
                    sortObj = Sort.by(direction, sortParams[0]);
                } else if (sortParams.length == 1) {
                    // Mặc định ASC nếu chỉ có tên trường
                    sortObj = Sort.by(Sort.Direction.ASC, sortParams[0]);
                }
            } catch (Exception e) {
                System.err.println("Invalid sort parameter format: " + sort);
                // xem xét trả về lỗi hoặc dùng sort mặc định
            }
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);

        // --- Phần gọi Service và thêm dữ liệu phim vào model giữ nguyên ---
    
        Page<Movie> moviePage = movieService.findMovies(pageable, query );

        model.addAttribute("moviesPage", moviePage);
        model.addAttribute("movies", moviePage.getContent()); // Bạn có thể giữ lại nếu template cũ dùng

        // --- Thêm các tham số phân trang/sắp xếp/tìm kiếm vào model ---
        model.addAttribute("currentPage", page); // Đảm bảo template dùng đúng tên này
        model.addAttribute("pageSize", size);
        model.addAttribute("sortParam", sort);
        model.addAttribute("queryParam", query);

        // ====> 2. Thêm requestURI vào model <====
        model.addAttribute("requestURI", request.getRequestURI());
        // ======================================

        return "movielist"; // Trả về tên file template (movielist.html)
    }


//    @GetMapping("/")
//    public String homePage() {
//         // Chuyển hướng về trang danh sách phim làm trang chủ
//         return "redirect:/movielist";
//    }
}
