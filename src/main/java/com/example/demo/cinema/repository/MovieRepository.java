package com.example.demo.cinema.repository;

import com.example.demo.cinema.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> { 

    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

}
