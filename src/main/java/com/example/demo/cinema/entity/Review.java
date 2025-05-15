package com.example.demo.cinema.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 255)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Integer likes = 0;

    @Column(nullable = false)
    private Integer dislikes = 0;

    @Column(nullable = false)
    private Boolean verified = false;

    
    @Column(name = "report_count", nullable = false)
    private Integer reportCount = 0; 
    
    
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ReviewVote> votes = new HashSet<>();

   
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ReviewReport> reports = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
        if (this.likes == null) this.likes = 0;
        if (this.dislikes == null) this.dislikes = 0;
        if (this.verified == null) this.verified = false;
        if (this.reportCount == null) this.reportCount = 0; 
    }

    
    public Review() {}

   
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }
    public Integer getDislikes() { return dislikes; }
    public void setDislikes(Integer dislikes) { this.dislikes = dislikes; }
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }

    public Set<ReviewVote> getVotes() { return votes; }
    public void setVotes(Set<ReviewVote> votes) { this.votes = votes; }

    
    public Integer getReportCount() {
        return reportCount;
    }

    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
    }

    public Set<ReviewReport> getReports() {
        return reports;
    }

    public void setReports(Set<ReviewReport> reports) {
        this.reports = reports;
    }

    
    public void addVote(ReviewVote vote) {
        if (vote != null) {
            this.votes.add(vote);
            vote.setReview(this);
        }
    }

    public void removeVote(ReviewVote vote) {
        if (vote != null) {
            this.votes.remove(vote);
            vote.setReview(null);
        }
    }

    
    public void addReport(ReviewReport report) {
        if (report != null) {
            this.reports.add(report);
            report.setReview(this);
        }
    }

    public void removeReport(ReviewReport report) {
        if (report != null) {
            this.reports.remove(report);
            report.setReview(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return id != null && Objects.equals(id, review.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}