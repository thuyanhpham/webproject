package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.repository.MovieRepository;
import com.example.demo.cinema.repository.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;

    @Autowired
    public MovieService(MovieRepository movieRepository, ShowtimeRepository showtimeRepository) {
        this.movieRepository = movieRepository;
        this.showtimeRepository = showtimeRepository;
    }

    @Transactional(readOnly = true)
    public Page<Movie> findMovies(Pageable pageable, String query) {
        if (StringUtils.hasText(query)) {
            return movieRepository.findByTitleContainingIgnoreCase(query, pageable);
        } else {
            return movieRepository.findAll(pageable);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Movie> findMovieByIdOptional(Long id) {
        return movieRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Movie findMovieByIdWithDetails(Long id) {
        Movie movie = movieRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));

        if (movie.getPhotoUrls() != null) {
            movie.getPhotoUrls().size();
        }
        if (movie.getAvailableFormats() != null) {
            movie.getAvailableFormats().size();
        }

        return movie;
    }

    @Transactional
    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie not found with id: " + id + ". Cannot delete.");
        }
        movieRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }
}

