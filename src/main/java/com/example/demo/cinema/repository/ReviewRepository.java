package com.example.demo.cinema.repository;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Tìm review theo Movie (hữu ích nếu không load cùng Movie)
    List<Review> findByMovie(Movie movie);

    // Tìm review theo Movie ID (thường dùng hơn)
    List<Review> findByMovieId(Long movieId);

    // Tìm review theo Movie ID có phân trang (cho nút "load more")
    Page<Review> findByMovieId(Long movieId, Pageable pageable);

    // Tìm review theo User ID (có thể cần cho trang profile user)
    // List<Review> findByUserId(Long userId);
}