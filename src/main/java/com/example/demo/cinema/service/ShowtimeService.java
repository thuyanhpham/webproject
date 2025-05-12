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

    // --- Phương thức Tạo mới Suất chiếu ---
    @Transactional // Đảm bảo tính toàn vẹn dữ liệu
    public Showtime createShowtime(Long movieId, Long roomId, LocalDate showDate, String startTimeStr, String experience, BigDecimal price) {
        log.info("Attempting to create showtime for movie ID: {}, room ID: {}, date: {}, time string: {}", movieId, roomId, showDate, startTimeStr);

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));
        movieRepository.saveAndFlush(movie);
        log.debug("Movie with ID {} explicitly flushed.", movie.getId());
        
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        LocalTime parsedStartTime;
        try {
            parsedStartTime = LocalTime.parse(startTimeStr);
        } catch (Exception e) {
            log.warn("Invalid start time format provided: {}", startTimeStr);
            throw new IllegalArgumentException("Invalid start time format. Please use HH:mm.");
        }

        if (movie.getDuration() == null || movie.getDuration() <= 0) {
            log.error("Movie ID {} has invalid duration: {}", movieId, movie.getDuration());
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
        showtime.setPrice(price);
        // createdAt, updatedAt sẽ tự động qua @PrePersist trong Entity

        Showtime savedShowtime = showtimeRepository.save(showtime);
        log.info("Showtime created successfully with ID: {}", savedShowtime.getId());
        return savedShowtime;
    }

    // --- Phương thức Sửa Suất chiếu ---
    @Transactional
    public Showtime updateShowtime(Long showtimeId, Long movieId, Long roomId, LocalDate showDate, String startTimeStr, String experience, BigDecimal price) {
        log.info("Attempting to update showtime ID: {}", showtimeId);

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
            log.warn("Invalid start time format provided for update: {}", startTimeStr);
            throw new IllegalArgumentException("Invalid start time format. Please use HH:mm.");
        }

        if (movie.getDuration() == null || movie.getDuration() <= 0) {
            log.error("Movie ID {} has invalid duration for update: {}", movieId, movie.getDuration());
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
        existingShowtime.setPrice(price);
        // updatedAt sẽ tự động qua @PreUpdate trong Entity

        Showtime updatedShowtime = showtimeRepository.save(existingShowtime);
        log.info("Showtime updated successfully for ID: {}", updatedShowtime.getId());
        return updatedShowtime;
    }

    // --- Helper method để kiểm tra xung đột ---
    private void validateShowtimeConflict(Long currentShowtimeIdToExclude, Long roomId, LocalDate showDateForValidation,
                                        LocalDateTime newStartDateTime, LocalDateTime newEndDateTime) {
        log.debug("Validating showtime conflict for room_id: {} on date: {}, new period: {} - {}, excluding ID: {}",
                roomId, showDateForValidation, newStartDateTime, newEndDateTime, currentShowtimeIdToExclude);

        // Lấy tất cả suất chiếu trong cùng phòng và ngày
        List<Showtime> showtimesInRoomOnDate = showtimeRepository.findByRoomIdAndShowDateOrderByStartTimeAsc(roomId, showDateForValidation);

        for (Showtime existing : showtimesInRoomOnDate) {
            if (currentShowtimeIdToExclude != null && existing.getId().equals(currentShowtimeIdToExclude)) {
                continue; // Bỏ qua chính suất chiếu đang sửa
            }

            // Tính toán startDateTime và endDateTime cho suất chiếu hiện có từ DB
            Movie existingMovie = existing.getMovie();
            if (existingMovie == null || existingMovie.getDuration() == null || existingMovie.getDuration() <= 0) {
                 log.warn("Skipping conflict check for existing showtime ID {} due to missing movie or duration.", existing.getId());
                 continue;
            }
            LocalDateTime existingStartDateTime = LocalDateTime.of(existing.getShowDate(), existing.getStartTime());
            LocalDateTime existingEndDateTime = existingStartDateTime.plusMinutes(existingMovie.getDuration());

            // Kiểm tra chồng chéo thời gian
            if (newStartDateTime.isBefore(existingEndDateTime) && newEndDateTime.isAfter(existingStartDateTime)) {
                String errorMessage = "Showtime conflict with existing showtime (ID: " + existing.getId() + ")" +
                                      " from " + existingStartDateTime.toLocalTime().toString() + // Thêm toString() cho rõ
                                      " to " + existingEndDateTime.toLocalTime().toString() +   // Thêm toString() cho rõ
                                      " in the same room on " + showDateForValidation + ".";
                log.warn(errorMessage);
                throw new IllegalArgumentException(errorMessage); // Sử dụng IllegalArgumentException
            }
        }
        log.debug("No showtime conflicts found for room_id: {} on date: {}", roomId, showDateForValidation);
    }


    // --- Phương thức Xóa Suất chiếu ---
    @Transactional
    public void deleteShowtime(Long showtimeId) {
        log.warn("Attempting to delete showtime_id: {}", showtimeId);
        if (!showtimeRepository.existsById(showtimeId)) {
            throw new ResourceNotFoundException("Showtime not found with id: " + showtimeId + ". Cannot delete.");
        }
        // Cân nhắc kiểm tra xem suất chiếu này có vé đã đặt không trước khi xóa (logic nghiệp vụ phức tạp hơn)
        showtimeRepository.deleteById(showtimeId);
        log.info("Showtime deleted successfully with ID: {}", showtimeId);
    }

    // --- Các phương thức Tìm kiếm ---
    @Transactional(readOnly = true)
    public Optional<Showtime> findById(Long id) {
        return showtimeRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Showtime> findAllShowtimes(Pageable pageable) {
        return showtimeRepository.findAll(pageable);
    }

    // Tìm suất chiếu của phim trong một ngày cụ thể
    @Transactional(readOnly = true)
    public List<Showtime> getShowtimesByMovieAndDate(Long movieId, LocalDate showDate) {
        log.debug("Fetching showtimes for movie_id: {} on date: {}", movieId, showDate);
        return showtimeRepository.findByMovieIdAndShowDateOrderByStartTimeAsc(movieId, showDate);
    }

    // Tìm các ngày có suất chiếu của một bộ phim
    @Transactional(readOnly = true)
    public List<LocalDate> findDistinctShowDatesByMovieId(Long movieId) {
        log.debug("Fetching distinct show dates for movie_id: {}", movieId);
        return showtimeRepository.findDistinctShowDatesByMovieId(movieId);
    }
    
    @Transactional(readOnly = true)
    public List<LocalDate> findAvailableDatesForMovie(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            log.warn("Attempted to find available dates for non-existent movie ID: {}", movieId);
            return Collections.emptyList(); // Hoặc ném ResourceNotFoundException nếu muốn
        }
        return showtimeRepository.findDistinctShowDatesByMovieIdAndDateAfterOrEqual(movieId);
    }
    
    @Transactional(readOnly = true)
    public List<String> findAvailableExperiencesForMovieAndDate(Long movieId, LocalDate date) {
        if (!movieRepository.existsById(movieId)) {
            log.warn("Attempted to find available experiences for non-existent movie ID: {}", movieId);
            return Collections.emptyList();
        }
        if (date == null) {
            log.warn("Date parameter is null for findAvailableExperiencesForMovieAndDate, movieId: {}", movieId);
            return Collections.emptyList();
        }
        return showtimeRepository.findDistinctExperiencesByMovieIdAndShowDate(movieId, date);
    }
    
    @Transactional(readOnly = true)
    public List<Showtime> findAvailableShowtimes(Long movieId, LocalDate date, String experience) {
        if (!movieRepository.existsById(movieId)) {
            log.warn("Attempted to find available showtimes for non-existent movie ID: {}", movieId);
            return Collections.emptyList();
        }
        if (date == null) {
            log.warn("Date parameter is null for findAvailableShowtimes, movieId: {}", movieId);
            return Collections.emptyList();
        }
        return showtimeRepository.findShowtimesByMovieIdAndShowDateAndExperience(movieId, date, experience);
    }
}