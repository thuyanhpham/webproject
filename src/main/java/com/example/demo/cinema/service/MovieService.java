package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.entity.MovieStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MovieService {
	
	private static final Logger log = LoggerFactory.getLogger(MovieService.class);

    private final MovieRepository movieRepository;

    @Autowired
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Transactional(readOnly = true)
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Movie> getAllMoviesPaginated(Pageable pageable) {
        Page<Movie> result = movieRepository.findAll(pageable);
        return result;
    }

    @Transactional(readOnly = true)
    public Page<Movie> searchMovies(String searchTerm, Pageable pageable) {
        Page<Movie> result = movieRepository.findByTitleContainingIgnoreCase(searchTerm, pageable);
        return result;
    }
    
    @Transactional(readOnly = true)
    public Optional<Movie> findMovieByIdOptional(Long id) {
        return movieRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Page<Movie> findMovies(Pageable pageable, String searchTerm) {
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            return movieRepository.findByTitleContainingIgnoreCase(searchTerm.trim(), pageable);
        } else {
            return movieRepository.findAll(pageable);
        }
    }

    @Transactional(readOnly = true)
    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> {
                     return new ResourceNotFoundException("Movie not found with id: " + id);
                });
    }

    @Transactional 
    public Movie createNewMovie(Movie movieDataFromForm) {
        movieDataFromForm.setId(null);
        Movie savedMovie = movieRepository.save(movieDataFromForm);
        if (savedMovie.getId() == null) {
            throw new IllegalStateException("Failed to generate movie ID after initial save.");
        }
        return savedMovie;
    }

    @Transactional
    public Movie updateMovie(Long movieId, Movie movieDataFromForm) {
        Movie existingMovie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId + " for update."));

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
        existingMovie.setFormat(movieDataFromForm.getFormat());

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
    public List<Movie> findNowShowingMovies() {
        LocalDate today = LocalDate.now();
        return movieRepository.findNowShowingMoviesByStatus(today, MovieStatus.NOW_SHOWING);
    }

    @Transactional(readOnly = true)
    public List<Movie> findComingSoonMovies() {
        LocalDate today = LocalDate.now();
        return movieRepository.findByReleaseDateGreaterThanOrderByReleaseDateAsc(today);
    }
 }
