package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.entity.Review;
import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.entity.VoteType;
import com.example.demo.cinema.entity.ReviewVote;
import com.example.demo.cinema.entity.ReviewReport; 
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.repository.MovieRepository;
import com.example.demo.cinema.repository.ReviewRepository;
import com.example.demo.cinema.repository.UserRepository;
import com.example.demo.cinema.repository.ReviewVoteRepository;
import com.example.demo.cinema.repository.ReviewReportRepository; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class ReviewService {

	private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final ReviewVoteRepository reviewVoteRepository;
    private final ReviewReportRepository reviewReportRepository; 

    private static final int MAX_REPORTS_BEFORE_DELETION = 5; 

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         MovieRepository movieRepository,
                         ReviewVoteRepository reviewVoteRepository,
                         ReviewReportRepository reviewReportRepository) { 
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.reviewVoteRepository = reviewVoteRepository;
        this.reviewReportRepository = reviewReportRepository; 
    }

    @Transactional(readOnly = true)
    public Page<Review> getReviewsForMovie(Long movieId, Pageable pageable) {
        log.debug("Fetching reviews for movie ID: {} with pageable: {}", movieId, pageable);
        Page<Review> reviewPage = reviewRepository.findByMovieIdWithUserFetched(movieId, pageable);
        log.debug("Found {} reviews for movie ID: {} on page: {}", reviewPage.getNumberOfElements(), movieId, pageable.getPageNumber());
        return reviewPage;
    }

    @Transactional
    public Review createReview(String username, Long movieId, Integer rating, String title, String content) {
        log.debug("Attempting to create review by user: {} for movie ID: {}", username, movieId);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", username);
                    return new ResourceNotFoundException("User not found with username: " + username);
                });

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> {
                    log.error("Movie not found with ID: {} for review creation", movieId);
                    return new ResourceNotFoundException("Movie not found with ID: " + movieId);
                });

        if (reviewRepository.findByUserAndMovie(user, movie).isPresent()) {
            log.warn("User {} has already reviewed movie ID {}", username, movieId);
            throw new DataIntegrityViolationException("You have already reviewed this movie.");
        }

        Review review = new Review();
        review.setUser(user);
        review.setMovie(movie);
        review.setRating(rating);
        review.setTitle(title);
        review.setContent(content);
        review.setVerified(false);

        Review savedReview = reviewRepository.save(review);
        log.info("Review created successfully with ID: {} by user: {} for movie ID: {}", savedReview.getId(), username, movieId);
        return savedReview;
    }

    @Transactional
    public Review handleLikeReview(Long reviewId, String usernameOfLiker) {
        log.debug("User {} attempting to LIKE review ID: {}", usernameOfLiker, reviewId);
        User user = userRepository.findByUsername(usernameOfLiker)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + usernameOfLiker));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        if (review.getUser().equals(user)) {
            log.warn("User {} cannot like their own review (ID: {})", usernameOfLiker, reviewId);
            throw new IllegalStateException("You cannot like your own review.");
        }

        Optional<ReviewVote> existingVoteOpt = reviewVoteRepository.findByUserAndReview(user, review);

        if (existingVoteOpt.isPresent()) {
            ReviewVote existingVote = existingVoteOpt.get();
            if (existingVote.getVoteType() == VoteType.LIKE) {
                reviewVoteRepository.delete(existingVote);
                log.info("User {} UNLIKED review ID: {}", usernameOfLiker, reviewId);
            } else {
                existingVote.setVoteType(VoteType.LIKE);
                reviewVoteRepository.save(existingVote);
                log.info("User {} changed vote to LIKE for review ID: {}", usernameOfLiker, reviewId);
            }
        } else {
            ReviewVote newVote = new ReviewVote(user, review, VoteType.LIKE);
            reviewVoteRepository.save(newVote);
            log.info("User {} LIKED review ID: {}", usernameOfLiker, reviewId);
        }
        updateReviewLikeDislikeCounts(review);
        return reviewRepository.save(review);
    }

    @Transactional
    public Review handleDislikeReview(Long reviewId, String usernameOfDisliker) {
        log.debug("User {} attempting to DISLIKE review ID: {}", usernameOfDisliker, reviewId);
        User user = userRepository.findByUsername(usernameOfDisliker)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + usernameOfDisliker));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        if (review.getUser().equals(user)) {
            log.warn("User {} cannot dislike their own review (ID: {})", usernameOfDisliker, reviewId);
            throw new IllegalStateException("You cannot dislike your own review.");
        }

        Optional<ReviewVote> existingVoteOpt = reviewVoteRepository.findByUserAndReview(user, review);

        if (existingVoteOpt.isPresent()) {
            ReviewVote existingVote = existingVoteOpt.get();
            if (existingVote.getVoteType() == VoteType.DISLIKE) {
                reviewVoteRepository.delete(existingVote);
                log.info("User {} UNDISLIKED review ID: {}", usernameOfDisliker, reviewId);
            } else {
                existingVote.setVoteType(VoteType.DISLIKE);
                reviewVoteRepository.save(existingVote);
                log.info("User {} changed vote to DISLIKE for review ID: {}", usernameOfDisliker, reviewId);
            }
        } else {
            ReviewVote newVote = new ReviewVote(user, review, VoteType.DISLIKE);
            reviewVoteRepository.save(newVote);
            log.info("User {} DISLIKED review ID: {}", usernameOfDisliker, reviewId);
        }
        updateReviewLikeDislikeCounts(review);
        return reviewRepository.save(review);
    }

    private void updateReviewLikeDislikeCounts(Review review) {
        if (review == null || review.getId() == null) {
            log.warn("Attempted to update counts for a null review or review with null ID.");
            return;
        }
        Review freshReview = reviewRepository.findById(review.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Review not found for count update: " + review.getId()));
        long likesCount = reviewVoteRepository.countByReviewAndVoteType(freshReview, VoteType.LIKE);
        long dislikesCount = reviewVoteRepository.countByReviewAndVoteType(freshReview, VoteType.DISLIKE);
        freshReview.setLikes((int) likesCount);
        freshReview.setDislikes((int) dislikesCount);
        log.debug("Updated counts for review ID {}: Likes = {}, Dislikes = {}", freshReview.getId(), likesCount, dislikesCount);
    }

    
    @Transactional
    public Map<String, Object> handleReportAbuse(Long reviewId, String reportingUsername) {
        //log.debug("User {} attempting to REPORT review ID: {}", reportingUsername, reviewId);
        User reporter = userRepository.findByUsername(reportingUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + reportingUsername));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        
        if (review.getUser().equals(reporter)) {
            //log.warn("User {} cannot report their own review (ID: {})", reportingUsername, reviewId);
            throw new IllegalStateException("You cannot report your own review.");
        }

        Optional<ReviewReport> existingReportOpt = reviewReportRepository.findByUserAndReview(reporter, review);

        if (existingReportOpt.isPresent()) {
            //log.info("User {} has already reported review ID: {}", reportingUsername, reviewId);
           
            return Map.of(
                "message", "You have already reported this review.",
                "reviewId", review.getId(),
                "reportCount", review.getReportCount(), // Gửi lại reportCount hiện tại
                "deleted", false
            );
        }

        
        ReviewReport newReport = new ReviewReport(reporter, review);
        reviewReportRepository.save(newReport);
        log.info("User {} REPORTED review ID: {}", reportingUsername, reviewId);
        long currentReportCount = reviewReportRepository.countByReview(review);
        review.setReportCount((int) currentReportCount);
        

        log.info("Review ID: {} now has {} reports.", reviewId, review.getReportCount());

        if (review.getReportCount() > MAX_REPORTS_BEFORE_DELETION) {
            log.warn("Review ID: {} has {} reports, exceeding threshold of {}. Deleting review.",
                    reviewId, review.getReportCount(), MAX_REPORTS_BEFORE_DELETION);
            reviewRepository.delete(review);
            
            return Map.of(
                "message", "Review reported and has been deleted due to exceeding report threshold.",
                "reviewId", reviewId, 
                "deleted", true
            );
        } else {
            reviewRepository.save(review); 
            return Map.of(
                "message", "Review reported successfully.",
                "reviewId", review.getId(),
                "reportCount", review.getReportCount(),
                "deleted", false
            );
        }
    }
}
