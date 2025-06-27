package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.entity.MovieCrew;
import com.example.demo.cinema.entity.MovieStatus;
import com.example.demo.cinema.entity.Person;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.repository.MovieCrewRepository;
import com.example.demo.cinema.repository.MovieRepository;
import com.example.demo.cinema.repository.PersonRepository;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class MovieService {
	
	private static final Logger log = LoggerFactory.getLogger(MovieService.class);

    private final MovieRepository movieRepository;
    private final PersonRepository personRepository;
    private final MovieCrewRepository movieCrewRepository;

    @Value("${app.upload.movie-posters.dir}")
    private String moviePostersUploadDir;
    private Path moviePostersUploadPath;

    @Autowired
    public MovieService(MovieRepository movieRepository, PersonRepository personRepository, MovieCrewRepository movieCrewRepository) {
        this.movieRepository = movieRepository;
        this.personRepository = personRepository;
        this.movieCrewRepository = movieCrewRepository;
    }

    @PostConstruct
    public void init() {
        this.moviePostersUploadPath = Paths.get(moviePostersUploadDir);
        try {
            Files.createDirectories(moviePostersUploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory for movie posters!", e);
        }
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
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }

    @Transactional
    public Movie saveMovieAndCrew(Movie movie, MultipartFile posterFile, List<Long> directorIds, List<Long> actorIds) {
        
        handlePosterUpload(movie, posterFile);
        
        if (movie.getId() != null) {
            movieCrewRepository.deleteByMovieId(movie.getId());
        }

        Movie savedMovie = movieRepository.save(movie);

        Set<MovieCrew> newCrewSet = new HashSet<>();

        if (directorIds != null && !directorIds.isEmpty()) {
            List<Person> directors = personRepository.findAllById(directorIds);
            for (Person person : directors) {
                MovieCrew crew = new MovieCrew();
                crew.setMovie(savedMovie);
                crew.setPerson(person);
                crew.setRole(Movie.ROLE_DIRECTOR);
                newCrewSet.add(crew);
            }
        }
        
        if (actorIds != null && !actorIds.isEmpty()) {
            List<Person> actors = personRepository.findAllById(actorIds);
            for (Person person : actors) {
                MovieCrew crew = new MovieCrew();
                crew.setMovie(savedMovie);
                crew.setPerson(person);
                crew.setRole(Movie.ROLE_ACTOR);
                newCrewSet.add(crew);
            }
        }
        
        if (!newCrewSet.isEmpty()) {
            movieCrewRepository.saveAll(newCrewSet);
        }
        
        savedMovie.getCrew().clear();
        savedMovie.getCrew().addAll(newCrewSet);
        
        return savedMovie;
    }


    @Transactional
    public void deleteMovie(Long id) {
        Movie movieToDelete = getMovieById(id);
        deleteAssociatedPoster(movieToDelete.getPosterUrl());
        movieRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Movie> findNowShowingMovies() {
        LocalDate today = LocalDate.now();
        return movieRepository.findByStatusAndReleaseDateLessThanEqualOrderByReleaseDateDesc(
                MovieStatus.NOW_SHOWING, today);
    }

    @Transactional(readOnly = true)
    public List<Movie> findComingSoonMovies() {
        LocalDate today = LocalDate.now();
        return movieRepository.findByStatusAndReleaseDateGreaterThanOrderByReleaseDateAsc(
                MovieStatus.COMING_SOON, today);
    }

    private void handlePosterUpload(Movie movie, MultipartFile posterFile) {
        if (posterFile == null || posterFile.isEmpty()) {
            return;
        }
        if (movie.getId() != null) {
            movieRepository.findById(movie.getId()).ifPresent(oldMovie -> 
                deleteAssociatedPoster(oldMovie.getPosterUrl())
            );
        }      
        String uniqueFileName = generateUniqueFileName(posterFile.getOriginalFilename());
        try {
            Files.copy(posterFile.getInputStream(), this.moviePostersUploadPath.resolve(uniqueFileName));
            movie.setPosterUrl("/movie-posters/" + uniqueFileName);
        } catch (IOException e) {
            throw new RuntimeException("Could not save poster file: " + e.getMessage());
        }
    }

    private void deleteAssociatedPoster(String posterUrl) {
        if (posterUrl == null || posterUrl.isBlank() || !posterUrl.startsWith("/movie-posters/")) {
            return;
        }
        try {
            String fileName = posterUrl.substring("/movie-posters/".length());
            Path posterPath = this.moviePostersUploadPath.resolve(fileName);
            Files.deleteIfExists(posterPath);
        } catch (IOException e) {
            log.error("Failed to delete poster file for URL {}: {}", posterUrl, e.getMessage());
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            originalFileName = "file";
        }
        String fileExtension = "";
        int lastDot = originalFileName.lastIndexOf(".");
        if (lastDot > 0) {
            fileExtension = originalFileName.substring(lastDot);
        }
        return UUID.randomUUID().toString() + fileExtension;
    }
 }
