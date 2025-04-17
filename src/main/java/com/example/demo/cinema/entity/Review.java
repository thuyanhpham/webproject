package com.example.demo.cinema.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mối quan hệ tới User (người viết review)
    @ManyToOne(fetch = FetchType.LAZY) // Chỉ tải User khi thực sự cần (ví dụ: review.getUser().getName())
    @JoinColumn(name = "user_id", nullable = false) // Khóa ngoại trong bảng reviews
    private User user;

    // Mối quan hệ tới Movie (phim được review)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false) // Khóa ngoại trong bảng reviews
    private Movie movie;

    @Column(nullable = false)
    private Integer rating; // Giả sử là điểm số sao (1-5)

    @Column(length = 255) // Tiêu đề review (có thể null)
    private String title;

    @Lob // Cho nội dung review dài
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false) // Thời gian tạo review
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Integer likes = 0; // Số lượt thích, mặc định là 0

    @Column(nullable = false)
    private Integer dislikes = 0; // Số lượt không thích, mặc định là 0

    @Column(nullable = false)
    private Boolean verified = false; // Đánh dấu review đã được xác minh (ví dụ: user đã xem phim)

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
        // Đảm bảo likes/dislikes/verified có giá trị mặc định nếu là null
        if (this.likes == null) this.likes = 0;
        if (this.dislikes == null) this.dislikes = 0;
        if (this.verified == null) this.verified = false;
    }

    // Constructors
    public Review() {}

    // Getters and Setters
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

    // equals() and hashCode() based on id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return id != null && Objects.equals(id, review.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode(); // Hoặc Objects.hash(id) nếu id luôn khác null sau khi persist
    }
}