package com.example.demo.cinema.repository; // Đảm bảo package này đúng với cấu trúc dự án của bạn

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