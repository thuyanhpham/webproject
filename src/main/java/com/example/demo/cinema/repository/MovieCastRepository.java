package com.example.demo.cinema.repository;

import com.example.demo.cinema.entity.MovieCast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieCastRepository extends JpaRepository<MovieCast, Long> {
    // Tìm các vai diễn của một phim
    List<MovieCast> findByMovieId(Long movieId);

    // Tìm các phim mà một diễn viên tham gia
    List<MovieCast> findByCastMemberId(Long castMemberId);
}
