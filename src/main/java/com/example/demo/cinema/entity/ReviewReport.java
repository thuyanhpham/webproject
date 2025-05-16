package com.example.demo.cinema.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "review_reports",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "review_id"})) 
public class ReviewReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false) // Review bị report
    private Review review;

    @Column(name = "reported_at", nullable = false, updatable = false)
    private LocalDateTime reportedAt;

    @Column(length = 500)
    private String reason;

    @PrePersist
    protected void onCreate() {
        reportedAt = LocalDateTime.now();
    }

    // Constructors
    public ReviewReport() {
    }

    public ReviewReport(User user, Review review) {
        this.user = user;
        this.review = review;
    }

    public ReviewReport(User user, Review review, String reason) {
        this.user = user;
        this.review = review;
        this.reason = reason;
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Review getReview() { return review; }
    public void setReview(Review review) { this.review = review; }
    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviewReport that = (ReviewReport) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

