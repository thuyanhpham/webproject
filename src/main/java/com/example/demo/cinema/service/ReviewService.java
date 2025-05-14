package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.entity.Review;
import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.repository.MovieRepository; // Cần để lấy Movie nếu controller chỉ truyền ID
import com.example.demo.cinema.repository.ReviewRepository;
import com.example.demo.cinema.repository.UserRepository; // Cần để lấy User object

// Import BookingRepository và BookingStatus nếu bạn triển khai logic "verified" hoặc "userHasWatchedMovie"
// import com.example.demo.cinema.repository.BookingRepository;
// import com.example.demo.cinema.entity.BookingStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException; // Để bắt lỗi nếu user đã review
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

	private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository; // Chỉ cần nếu controller truyền movieId thay vì Movie object
    // private final BookingRepository bookingRepository; // Bỏ comment nếu dùng

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         MovieRepository movieRepository
                         /*, BookingRepository bookingRepository */) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        // this.bookingRepository = bookingRepository;
    }

    // Lấy các review cho một phim với phân trang (và fetch User)
    @Transactional(readOnly = true)
    public Page<Review> getReviewsForMovie(Long movieId, Pageable pageable) {
        log.debug("Fetching reviews for movie ID: {} with pageable: {}", movieId, pageable);
        // Sử dụng phương thức có JOIN FETCH user từ ReviewRepository
        Page<Review> reviewPage = reviewRepository.findByMovieIdWithUserFetched(movieId, pageable);
        log.debug("Found {} reviews for movie ID: {} on page: {}", reviewPage.getNumberOfElements(), movieId, pageable.getPageNumber());
        return reviewPage;
    }

    // Tạo review mới
    @Transactional
    public Review createReview(String username, Long movieId, Integer rating, String title, String content) {
        log.debug("Attempting to create review by user: {} for movie ID: {}", username, movieId);

        User user = userRepository.findByUsername(username) // Hoặc findByEmail tùy theo cách bạn định danh user
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", username);
                    return new ResourceNotFoundException("User not found with username: " + username);
                });

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> {
                    log.error("Movie not found with ID: {} for review creation", movieId);
                    return new ResourceNotFoundException("Movie not found with ID: " + movieId);
                });

        // (Tùy chọn) Kiểm tra xem user đã review phim này chưa
        // Nếu bạn muốn mỗi user chỉ review 1 lần/phim, bạn cần unique constraint trong DB (user_id, movie_id)
        // và có thể bắt DataIntegrityViolationException hoặc kiểm tra trước:
        if (reviewRepository.findByUserAndMovie(user, movie).isPresent()) {
            log.warn("User {} has already reviewed movie ID {}", username, movieId);
            throw new DataIntegrityViolationException("You have already reviewed this movie.");
        }

        // (Tùy chọn) Logic cho 'verified' hoặc 'userHasWatchedMovie'
        // boolean isVerifiedOrHasWatched = false;
        // if (bookingRepository != null) {
        //     isVerifiedOrHasWatched = bookingRepository.existsByUserAndMovieAndStatus(user, movie, BookingStatus.COMPLETED);
        // }

        Review review = new Review();
        review.setUser(user);
        review.setMovie(movie);
        review.setRating(rating);
        review.setTitle(title);
        review.setContent(content);
        review.setVerified(false); // Mặc định, hoặc set dựa trên isVerifiedOrHasWatched
        // review.setUserHasWatchedMovie(isVerifiedOrHasWatched); // Nếu dùng trường này

        // timestamp, likes, dislikes sẽ được @PrePersist xử lý

        Review savedReview = reviewRepository.save(review);
        log.info("Review created successfully with ID: {} by user: {} for movie ID: {}", savedReview.getId(), username, movieId);
        return savedReview;
    }
}
