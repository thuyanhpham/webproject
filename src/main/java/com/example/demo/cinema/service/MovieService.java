package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.Movie;
// Bỏ import Showtime vì không dùng trực tiếp ở đây
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.repository.MovieRepository;
// Bỏ import ShowtimeRepository nếu không dùng ở đây

import org.hibernate.Hibernate; // Import Hibernate nếu dùng initialize
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MovieService { // Đổi tên class từ MovieService thành MovieServiceImpl nếu đây là class cài đặt

    private final MovieRepository movieRepository;

    // Nên dùng constructor injection cho tất cả dependencies
    @Autowired
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }
    // Bỏ @Autowired ShowtimeRepository nếu không dùng trong class này

    // --- Các phương thức hiện có ---

    @Transactional(readOnly = true)
    public List<Movie> getAllMovies() {
        // Phương thức này có thể không cần thiết cho HomeController mới,
        // nhưng giữ lại nếu có nơi khác dùng.
        return movieRepository.findAll();
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
        // *** QUAN TRỌNG: Đảm bảo phương thức findByIdWithDetails tồn tại trong
        // MovieRepository ***
        Movie movie = movieRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));

        // === Xử lý Lazy Loading cho @ElementCollection ===
        // Cách 1: Dùng Hibernate.initialize (Cần import org.hibernate.Hibernate)
        // Hibernate.initialize(movie.getPhotoUrls());
        // Hibernate.initialize(movie.getAvailableFormats());

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

    // --- PHƯƠNG THỨC MỚI CẦN THÊM ---

    /**
     * Lấy danh sách phim đang chiếu (ngày phát hành <= hôm nay).
     * 
     * @return List<Movie> phim đang chiếu.
     */
    @Transactional(readOnly = true)
    public List<Movie> findNowShowingMovies() {
        LocalDate today = LocalDate.now();
        // *** QUAN TRỌNG: Đảm bảo phương thức
        // findByReleaseDateLessThanEqualOrderByReleaseDateDesc tồn tại trong
        // MovieRepository ***
        return movieRepository.findByReleaseDateLessThanEqualOrderByReleaseDateDesc(today);
    }

    /**
     * Lấy danh sách phim sắp chiếu (ngày phát hành > hôm nay).
     * 
     * @return List<Movie> phim sắp chiếu.
     */
    @Transactional(readOnly = true)
    public List<Movie> findComingSoonMovies() {
        LocalDate today = LocalDate.now();
        // *** QUAN TRỌNG: Đảm bảo phương thức
        // findByReleaseDateGreaterThanOrderByReleaseDateAsc tồn tại trong
        // MovieRepository ***
        return movieRepository.findByReleaseDateGreaterThanOrderByReleaseDateAsc(today);
    }

    @Transactional(readOnly = true)
    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id));
    }
}
