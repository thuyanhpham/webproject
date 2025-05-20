package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.Showtime;
import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.entity.Room;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.repository.ShowtimeRepository;
import com.example.demo.cinema.repository.MovieRepository;
import com.example.demo.cinema.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ShowtimeService {

    private static final Logger log = LoggerFactory.getLogger(ShowtimeService.class);

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    @Autowired
    public ShowtimeService(ShowtimeRepository showtimeRepository,
                           MovieRepository movieRepository,
                           RoomRepository roomRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
    }

    public Showtime createShowtime(Long movieId, Long roomId, LocalDate showDate, String startTimeStr, String experience, BigDecimal price) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));
        movieRepository.saveAndFlush(movie);
        
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        LocalTime parsedStartTime;
        try {
            parsedStartTime = LocalTime.parse(startTimeStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid start time format. Please use HH:mm.");
        }

        if (movie.getDuration() == null || movie.getDuration() <= 0) {
            throw new IllegalArgumentException("Movie duration is invalid or not set for movie ID: " + movieId);
        }

        LocalDateTime newShowStartDateTime = LocalDateTime.of(showDate, parsedStartTime);
        LocalDateTime newShowEndDateTime = newShowStartDateTime.plusMinutes(movie.getDuration());

        validateShowtimeConflict(null, room.getId(), showDate, newShowStartDateTime, newShowEndDateTime);

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setShowDate(showDate);
        showtime.setStartTime(parsedStartTime);
        showtime.setExperience(experience);

        Showtime savedShowtime = showtimeRepository.save(showtime);
        return savedShowtime;
    }

    @Transactional
    public Showtime updateShowtime(Long showtimeId, Long movieId, Long roomId, LocalDate showDate, String startTimeStr, String experience, BigDecimal price) {

        Showtime existingShowtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + showtimeId));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        LocalTime parsedStartTime;
        try {
            parsedStartTime = LocalTime.parse(startTimeStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid start time format. Please use HH:mm.");
        }

        if (movie.getDuration() == null || movie.getDuration() <= 0) {
            throw new IllegalArgumentException("Movie duration is invalid or not set for movie ID: " + movieId);
        }
        LocalDateTime newShowStartDateTime = LocalDateTime.of(showDate, parsedStartTime);
        LocalDateTime newShowEndDateTime = newShowStartDateTime.plusMinutes(movie.getDuration());

        validateShowtimeConflict(showtimeId, room.getId(), showDate, newShowStartDateTime, newShowEndDateTime);

        existingShowtime.setMovie(movie);
        existingShowtime.setRoom(room);
        existingShowtime.setShowDate(showDate);
        existingShowtime.setStartTime(parsedStartTime);
        existingShowtime.setExperience(experience);

        Showtime updatedShowtime = showtimeRepository.save(existingShowtime);
        return updatedShowtime;
    }

    private void validateShowtimeConflict(Long currentShowtimeIdToExclude, Long roomId, LocalDate showDateForValidation,
                                        LocalDateTime newStartDateTime, LocalDateTime newEndDateTime) {
        List<Showtime> showtimesInRoomOnDate = showtimeRepository.findByRoomIdAndShowDateOrderByStartTimeAsc(roomId, showDateForValidation);

        for (Showtime existing : showtimesInRoomOnDate) {
            if (currentShowtimeIdToExclude != null && existing.getId().equals(currentShowtimeIdToExclude)) {
                continue;
            }

            Movie existingMovie = existing.getMovie();
            if (existingMovie == null || existingMovie.getDuration() == null || existingMovie.getDuration() <= 0) {
                 continue;
            }
            LocalDateTime existingStartDateTime = LocalDateTime.of(existing.getShowDate(), existing.getStartTime());
            LocalDateTime existingEndDateTime = existingStartDateTime.plusMinutes(existingMovie.getDuration());

            if (newStartDateTime.isBefore(existingEndDateTime) && newEndDateTime.isAfter(existingStartDateTime)) {
                String errorMessage = "Showtime conflict with existing showtime (ID: " + existing.getId() + ")" +
                                      " from " + existingStartDateTime.toLocalTime().toString() + 
                                      " to " + existingEndDateTime.toLocalTime().toString() +  
                                      " in the same room on " + showDateForValidation + ".";
                throw new IllegalArgumentException(errorMessage); 
            }
        }
    }

    @Transactional
    public void deleteShowtime(Long id) {
    	Showtime showtime = showtimeRepository.findById(id) 
    	        .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + id + " or already deleted."));
    	showtimeRepository.delete(showtime);
    }

    @Transactional(readOnly = true)
    public Optional<Showtime> findById(Long id) {
        return showtimeRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Showtime> findAllShowtimes(Pageable pageable) {
        return showtimeRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Showtime> getShowtimesByMovieAndDate(Long movieId, LocalDate showDate) {
        return showtimeRepository.findByMovieIdAndShowDateOrderByStartTimeAsc(movieId, showDate);
    }

    @Transactional(readOnly = true)
    public List<LocalDate> findDistinctShowDatesByMovieId(Long movieId) {
        return showtimeRepository.findDistinctShowDatesByMovieId(movieId);
    }
    
    @Transactional(readOnly = true)
    public List<LocalDate> findAvailableDatesForMovie(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            return Collections.emptyList();
        }
        return showtimeRepository.findDistinctShowDatesByMovieIdAndDateAfterOrEqual(movieId);
    }
    
    @Transactional(readOnly = true)
    public List<String> findAvailableExperiencesForMovieAndDate(Long movieId, LocalDate date) {
        if (!movieRepository.existsById(movieId)) {
            return Collections.emptyList();
        }
        if (date == null) {
            return Collections.emptyList();
        }
        return showtimeRepository.findDistinctExperiencesByMovieIdAndShowDate(movieId, date);
    }
    
    @Transactional(readOnly = true)
    public List<Showtime> findAvailableShowtimes(Long movieId, LocalDate date, String experience) {
        if (!movieRepository.existsById(movieId)) {
            return Collections.emptyList();
        }
        if (date == null) {
            return Collections.emptyList();
        }
        return showtimeRepository.findShowtimesByMovieIdAndShowDateAndExperience(movieId, date, experience);
    }

    @Transactional
    public void cleanupOldShowtimesByMarkingAsDeleted() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        int updatedCount = showtimeRepository.markOldShowtimesAsDeleted(today, now);
        if (updatedCount > 0) {
        } 
    }
}