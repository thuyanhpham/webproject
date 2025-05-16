package com.example.demo.cinema.repository;

import com.example.demo.cinema.entity.Review;
import com.example.demo.cinema.entity.ReviewReport;
import com.example.demo.cinema.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
  
    Optional<ReviewReport> findByUserAndReview(User user, Review review); 
    long countByReview(Review review);

}