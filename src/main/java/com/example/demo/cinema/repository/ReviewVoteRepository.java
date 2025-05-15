package com.example.demo.cinema.repository;
import com.example.demo.cinema.entity.Review;
import com.example.demo.cinema.entity.ReviewVote;
import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.entity.VoteType; // Import VoteType
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {

    // Tìm một vote cụ thể của một user cho một review
    Optional<ReviewVote> findByUserAndReview(User user, Review review);

    // (Tùy chọn) Đếm số lượng vote theo loại cho một review
    long countByReviewAndVoteType(Review review, VoteType voteType);
}

