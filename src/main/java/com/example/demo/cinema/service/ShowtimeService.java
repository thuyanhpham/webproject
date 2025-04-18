package com.example.demo.cinema.service; // Thay đổi package nếu cần

import com.example.demo.cinema.entity.Movie; // Import Movie entity
import com.example.demo.cinema.entity.Showtime; // Import Showtime entity
import com.example.demo.cinema.exception.ResourceNotFoundException; // Import Exception
import com.example.demo.cinema.repository.MovieRepository;
import com.example.demo.cinema.repository.ShowtimeRepository; // Import Showtime Repo

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional
import org.springframework.util.StringUtils; // Import StringUtils để kiểm tra chuỗi rỗng

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;

    @Autowired // Constructor Injection
    public ShowtimeService(ShowtimeRepository showtimeRepository, MovieRepository movieRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
    }

    // --- Các phương thức hiện có (giữ nguyên) ---

    @Transactional(readOnly = true)
    public List<Showtime> findAvailableShowtimes(Long movieId, LocalDate showDate, String experience) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Cannot find showtimes for non-existent Movie with id: " + movieId);
        }
        LocalDate dateToFilter = (showDate != null) ? showDate : LocalDate.now();
        if (StringUtils.hasText(experience)) {
            return showtimeRepository.findByMovieIdAndShowDateAndExperienceIgnoreCaseOrderByStartTimeAsc(movieId, dateToFilter, experience);
        } else {
            return showtimeRepository.findByMovieIdAndShowDateOrderByStartTimeAsc(movieId, dateToFilter);
        }
    }

    @Transactional(readOnly = true)
    public List<LocalDate> findAvailableDatesForMovie(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Cannot find dates for non-existent Movie with id: " + movieId);
        }
        // *** QUAN TRỌNG: Đảm bảo phương thức findDistinctShowDatesByMovieId tồn tại trong ShowtimeRepository ***
        return showtimeRepository.findDistinctShowDatesByMovieId(movieId);
    }

    @Transactional(readOnly = true)
    public List<String> findAvailableExperiencesForMovieAndDate(Long movieId, LocalDate showDate) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Cannot find experiences for non-existent Movie with id: " + movieId);
        }
        LocalDate dateToFilter = (showDate != null) ? showDate : LocalDate.now();
        // *** QUAN TRỌNG: Đảm bảo phương thức findDistinctExperiencesByMovieIdAndShowDate tồn tại trong ShowtimeRepository ***
        return showtimeRepository.findDistinctExperiencesByMovieIdAndShowDate(movieId, dateToFilter);
    }

    @Transactional(readOnly = true)
    public Showtime findShowtimeById(Long showtimeId) {
        return showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + showtimeId));
    }

    @Transactional
    public Showtime saveShowtime(Showtime showtime) {
        if (showtime.getMovie() == null || !movieRepository.existsById(showtime.getMovie().getId())) {
             throw new IllegalArgumentException("Showtime must be associated with an existing Movie.");
        }
        return showtimeRepository.save(showtime);
    }

    @Transactional
    public void deleteShowtime(Long showtimeId) {
        if (!showtimeRepository.existsById(showtimeId)) {
            throw new ResourceNotFoundException("Showtime not found with id: " + showtimeId + ". Cannot delete.");
        }
        showtimeRepository.deleteById(showtimeId);
    }

    // --- PHƯƠNG THỨC MỚI CẦN THÊM CHO HOME CONTROLLER ---

    /**
     * Lấy danh sách tất cả các ngày (không trùng lặp) có suất chiếu
     * trong hệ thống, tính từ ngày hiện tại trở đi (hoặc theo logic khác).
     * Dùng cho dropdown chọn ngày trên trang chủ hoặc trang danh sách phim.
     *
     * @return Danh sách các LocalDate.
     */

    @Transactional(readOnly = true)
    public List<LocalDate> findAllAvailableShowtimeDates() {
        LocalDate today = LocalDate.now();
        // *** QUAN TRỌNG: Đảm bảo phương thức findDistinctShowDatesAfter tồn tại trong ShowtimeRepository ***
        // Giả sử bạn muốn lấy các ngày từ hôm nay trở đi
        return showtimeRepository.findDistinctShowDatesAfter(today);
        // Hoặc nếu chỉ muốn lấy tất cả ngày có trong DB (kể cả quá khứ):
        // return showtimeRepository.findAllDistinctShowDates(); // Cần tạo phương thức này trong repo
    }
}