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
        Page<Review> reviewPage = reviewRepository.findByMovieIdWithUserFetched(movieId, pageable);
        return reviewPage;
    }

    @Transactional
    public Review createReview(String username, Long movieId, Integer rating, String title, String content) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("User not found with username: " + username);
                });

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("Movie not found with ID: " + movieId);
                });

        if (reviewRepository.findByUserAndMovie(user, movie).isPresent()) {
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
        return savedReview;
    }

    @Transactional
    public Review handleLikeReview(Long reviewId, String usernameOfLiker) {
        User user = userRepository.findByUsername(usernameOfLiker)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + usernameOfLiker));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        if (review.getUser().equals(user)) {
            throw new IllegalStateException("You cannot like your own review.");
        }

        Optional<ReviewVote> existingVoteOpt = reviewVoteRepository.findByUserAndReview(user, review);

        if (existingVoteOpt.isPresent()) {
            ReviewVote existingVote = existingVoteOpt.get();
            if (existingVote.getVoteType() == VoteType.LIKE) {
                reviewVoteRepository.delete(existingVote);
            } else {
                existingVote.setVoteType(VoteType.LIKE);
                reviewVoteRepository.save(existingVote);
            }
        } else {
            ReviewVote newVote = new ReviewVote(user, review, VoteType.LIKE);
            reviewVoteRepository.save(newVote);
        }
        updateReviewLikeDislikeCounts(review);
        return reviewRepository.save(review);
    }

    @Transactional
    public Review handleDislikeReview(Long reviewId, String usernameOfDisliker) {
        User user = userRepository.findByUsername(usernameOfDisliker)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + usernameOfDisliker));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        if (review.getUser().equals(user)) {
            throw new IllegalStateException("You cannot dislike your own review.");
        }

        Optional<ReviewVote> existingVoteOpt = reviewVoteRepository.findByUserAndReview(user, review);

        if (existingVoteOpt.isPresent()) {
            ReviewVote existingVote = existingVoteOpt.get();
            if (existingVote.getVoteType() == VoteType.DISLIKE) {
                reviewVoteRepository.delete(existingVote);
            } else {
                existingVote.setVoteType(VoteType.DISLIKE);
                reviewVoteRepository.save(existingVote);
            }
        } else {
            ReviewVote newVote = new ReviewVote(user, review, VoteType.DISLIKE);
            reviewVoteRepository.save(newVote);
        }
        updateReviewLikeDislikeCounts(review);
        return reviewRepository.save(review);
    }

    private void updateReviewLikeDislikeCounts(Review review) {
        if (review == null || review.getId() == null) {
            return;
        }
        Review freshReview = reviewRepository.findById(review.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Review not found for count update: " + review.getId()));
        long likesCount = reviewVoteRepository.countByReviewAndVoteType(freshReview, VoteType.LIKE);
        long dislikesCount = reviewVoteRepository.countByReviewAndVoteType(freshReview, VoteType.DISLIKE);
        freshReview.setLikes((int) likesCount);
        freshReview.setDislikes((int) dislikesCount);
    }

    
    @Transactional
    public Map<String, Object> handleReportAbuse(Long reviewId, String reportingUsername) {
        User reporter = userRepository.findByUsername(reportingUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + reportingUsername));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        
        if (review.getUser().equals(reporter)) {
            throw new IllegalStateException("You cannot report your own review.");
        }

        Optional<ReviewReport> existingReportOpt = reviewReportRepository.findByUserAndReview(reporter, review);

        if (existingReportOpt.isPresent()) {   
            return Map.of(
                "message", "You have already reported this review.",
                "reviewId", review.getId(),
                "reportCount", review.getReportCount(),
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
