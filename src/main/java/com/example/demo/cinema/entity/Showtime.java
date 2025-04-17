package com.example.demo.cinema.entity;

import jakarta.persistence.*; 
import java.math.BigDecimal; 
import java.time.LocalDate; 
import java.time.LocalTime;  
import java.time.LocalDateTime; 
import java.util.Objects; 

@Entity 
@Table(name = "showtimes") 
public class Showtime {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    // Mối quan hệ ManyToOne: Nhiều Suất chiếu thuộc về một Phim
    @ManyToOne(fetch = FetchType.LAZY) // Chỉ tải Movie khi cần, tránh load thừa
    @JoinColumn(name = "movie_id", nullable = false) // Tên cột khóa ngoại, không được null
    private Movie movie;

    @Column(name = "show_date", nullable = false) // Cột ngày chiếu, không được null
    private LocalDate showDate;

    @Column(name = "start_time", nullable = false) // Cột giờ bắt đầu, không được null
    private LocalTime startTime;

    @Column(nullable = false, length = 50) // Cột định dạng/trải nghiệm, không được null
    private String experience; // Ví dụ: "2D", "3D", "IMAX", "English-2D"

    @Column(name = "screen_name", length = 100) // Tên phòng chiếu (ví dụ: "Screen 1"), có thể null nếu chỉ có 1 phòng
    private String screenName;

    @Column(precision = 10, scale = 2) // Giá vé, ví dụ: 120000.00
    private BigDecimal price;

    @Column(name = "created_at", nullable = false, updatable = false) // Thời gian tạo, không cập nhật
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false) // Thời gian cập nhật cuối
    private LocalDateTime updatedAt;

    // ----- Lifecycle Callbacks -----
    @PrePersist // Chạy trước khi lưu lần đầu
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate // Chạy trước khi cập nhật
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ----- Constructors -----
    public Showtime() {
        // Constructor mặc định cần thiết cho JPA
    }

    // Constructor với các trường cơ bản (tùy chọn)
    public Showtime(Movie movie, LocalDate showDate, LocalTime startTime, String experience) {
        this.movie = movie;
        this.showDate = showDate;
        this.startTime = startTime;
        this.experience = experience;
    }

    // ----- Getters and Setters -----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }
    public LocalDate getShowDate() { return showDate; }
    public void setShowDate(LocalDate showDate) { this.showDate = showDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ----- equals() and hashCode() -----
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Quan trọng: Dùng instanceof thay vì getClass() != o.getClass() để tương thích với proxy của JPA
        if (!(o instanceof Showtime showtime)) return false;
        // Chỉ so sánh ID nếu cả hai đều không null (đã được persist)
        return id != null && Objects.equals(id, showtime.id);
    }

    @Override
    public int hashCode() {
        // Dùng getClass().hashCode() để nhất quán với equals khi dùng proxy
        // Hoặc return Objects.hash(id) nếu id được đảm bảo không null khi thêm vào Set/Map
        return getClass().hashCode();
    }

    // ----- toString() (Tùy chọn, hữu ích cho debug) -----
    @Override
    public String toString() {
        return "Showtime{" +
               "id=" + id +
               ", movie=" + (movie != null ? movie.getTitle() : "null") + // Tránh NullPointerException
               ", showDate=" + showDate +
               ", startTime=" + startTime +
               ", experience='" + experience + '\'' +
               ", screenName='" + screenName + '\'' +
               '}';
    }
}