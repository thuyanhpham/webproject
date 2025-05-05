package com.example.demo.cinema.service;
import com.example.demo.cinema.entity.Movie;

import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.repository.MovieRepository;
import com.example.demo.cinema.repository.ShowtimeRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static final Logger log = LoggerFactory.getLogger(MovieService.class);

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
    public Movie getMovieById(Long id) {
        log.debug("Finding movie by ID: {}", id);
        return movieRepository.findById(id)
                .orElseThrow(() -> {
                     log.error("Movie not found with id: {}", id);
                     return new ResourceNotFoundException("Movie not found with id: " + id);
                });
    }

    @Transactional 
    public Movie createNewMovie(Movie movieDataFromForm) {
        log.info("Service: Creating new movie: {}", movieDataFromForm.getTitle());
        movieDataFromForm.setId(null);

        Movie savedMovie = movieRepository.save(movieDataFromForm);
        log.info("Service: Initial save complete. Generated Movie ID: {}", savedMovie.getId());

        if (savedMovie.getId() == null) {
            log.error("FATAL: Movie ID is null after initial save! Check ID generation strategy and DB constraints.");
            throw new IllegalStateException("Failed to generate movie ID after initial save.");
        }

        return savedMovie;
    }

    @Transactional
    public Movie updateMovie(Long movieId, Movie movieDataFromForm) {
        log.info("Service: Updating existing movie with ID: {}", movieId);

        Movie existingMovie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId + " for update."));

        log.debug("Service: Updating fields for movie ID: {}", movieId);
        existingMovie.setTitle(movieDataFromForm.getTitle());
        existingMovie.setDescription(movieDataFromForm.getDescription());
        existingMovie.setDuration(movieDataFromForm.getDuration());
        existingMovie.setReleaseDate(movieDataFromForm.getReleaseDate());
        existingMovie.setLanguage(movieDataFromForm.getLanguage());
        existingMovie.setDirector(movieDataFromForm.getDirector());
        existingMovie.setTrailerUrl(movieDataFromForm.getTrailerUrl());
        existingMovie.setPosterUrl(movieDataFromForm.getPosterUrl());
        existingMovie.setBannerUrl(movieDataFromForm.getBannerUrl());
        existingMovie.setCast(movieDataFromForm.getCast());
        existingMovie.setStatus(movieDataFromForm.getStatus());
        existingMovie.setRating(movieDataFromForm.getRating());
        existingMovie.setGenres(movieDataFromForm.getGenres());
        existingMovie.setAvailableFormats(movieDataFromForm.getAvailableFormats());

        log.info("Service: Explicit save/merge executed for movie ID {}. Transaction proceeding to commit.", movieId);
        return existingMovie;
    }

    @Transactional
     public Movie saveOrUpdate(Movie movieDataFromForm) {
         if (movieDataFromForm.getId() == null) {
             return createNewMovie(movieDataFromForm);
         } else {
             if (!movieRepository.existsById(movieDataFromForm.getId())) {
                  throw new ResourceNotFoundException("Movie not found with id: " + movieDataFromForm.getId() + " for update.");
             }
             return updateMovie(movieDataFromForm.getId(), movieDataFromForm);
         }
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

}

