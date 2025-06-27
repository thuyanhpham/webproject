package com.example.demo.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.cinema.entity.MovieCrew;

@Repository
public interface MovieCrewRepository extends JpaRepository<MovieCrew, Long> {
    @Modifying
    @Query("DELETE FROM MovieCrew mc WHERE mc.movie.id = :movieId")
    void deleteByMovieId(Long movieId);
}
