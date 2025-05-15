package com.example.demo.cinema.dto;

import java.time.LocalDateTime;

public class ReviewDTO {
	private Long id;
    private UserReviewDTO user; 
    private Integer rating;
    private String title;
    private String content;
    private LocalDateTime timestamp; // Giữ nguyên tên trường 'timestamp' như trong Review Entity
    private Integer likes;
    private Integer dislikes;
    private Boolean verified;

    // Constructors
    public ReviewDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserReviewDTO getUser() {
        return user;
    }

    public void setUser(UserReviewDTO user) {
        this.user = user;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Integer getDislikes() {
        return dislikes;
    }

    public void setDislikes(Integer dislikes) {
        this.dislikes = dislikes;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
}
