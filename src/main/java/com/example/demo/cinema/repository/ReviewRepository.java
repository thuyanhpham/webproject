package com.example.demo.cinema.repository;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.entity.Review;
import com.example.demo.cinema.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByMovie(Movie movie);

    List<Review> findByMovieId(Long movieId);
    
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.user WHERE r.movie.id = :movieId")
    Page<Review> findByMovieIdWithUserFetched(@Param("movieId") Long movieId, Pageable pageable);
    Optional<Review> findByUserAndMovie(User user, Movie movie);
}