package com.example.demo.cinema.entity;

import java.math.BigDecimal; // Import kiểu số thực chính xác
import java.time.LocalDate;
import java.time.LocalDateTime; // Import kiểu ngày giờ cho timestamps
import java.util.HashSet;     // Import HashSet cho Set
import java.util.Set;         // Import Set interface
import java.util.Objects;     // Import Objects cho equals/hashCode

import jakarta.persistence.CascadeType; // Import CascadeType
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;   // Import FetchType (LAZY/EAGER)
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;    
import jakarta.persistence.JoinTable;     // Import JoinTable (để định nghĩa bảng trung gian)
import jakarta.persistence.Lob;           // Import Lob (cho kiểu TEXT/BLOB)
import jakarta.persistence.ManyToMany;    // Import ManyToMany
import jakarta.persistence.PrePersist;    // Import PrePersist (cho callback trước khi lưu)
import jakarta.persistence.PreUpdate;     // Import PreUpdate (cho callback trước khi cập nhật)
import jakarta.persistence.Table;



@Entity
@Table(name = "movies") 
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer duration; 

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(nullable = false, length = 50)
    private String language;

   

    // --- Các trường còn lại từ schema ---
    @Column(nullable = false, length = 100)
    private String director;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String actors;

    @Column(nullable = false, precision = 2, scale = 1) // decimal(2,1)
    private BigDecimal rating; // Sử dụng BigDecimal cho decimal

    @Column(name = "trailer_url", nullable = false, length = 255)
    private String trailerUrl;

    @Column(name = "poster_url", nullable = false, length = 255)
    private String posterUrl;

    @Column(name = "created_at", nullable = false, updatable = false) // updatable=false để không cập nhật khi sửa Movie
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

 
    @ManyToMany(
        fetch = FetchType.LAZY, // Chỉ tải genres khi gọi getGenres() 
        cascade = {
            CascadeType.PERSIST, // Lưu Genre mới nếu nó được thêm vào Movie chưa lưu
            CascadeType.MERGE   // Cập nhật Genre nếu Movie được merge
            // KHÔNG CascadeType.REMOVE: Xóa Movie không nên xóa Genre dùng chung
        }
    )
    @JoinTable(
        name = "movie_genres", // *** Tên bảng trung gian trong CSDL của bạn ***
        joinColumns = @JoinColumn(name = "movie_id", referencedColumnName = "id"), 
        inverseJoinColumns = @JoinColumn(name = "genre_id", referencedColumnName = "id") 
    )
    private Set<Genre> genres = new HashSet<>(); // Sử dụng Set để tránh trùng lặp và khởi tạo

    // --- Lifecycle Callbacks cho createdAt và updatedAt ---
    @PrePersist // Chạy trước khi entity được lưu lần đầu
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now(); // Khởi tạo luôn updatedAt
    }

    @PreUpdate // Chạy trước khi entity được cập nhật
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

   
    public Movie() {
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public String getActors() { return actors; }
    public void setActors(String actors) { this.actors = actors; }
    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Getter và Setter cho Set<Genre>
    public Set<Genre> getGenres() {
        return genres;
    }
    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    
    // Giúp quản lý mối quan hệ hai chiều một cách nhất quán
    public void addGenre(Genre genre) {
        if (genre != null) {
            this.genres.add(genre);
            // Vì Genre có mappedBy, ta cũng cần thêm Movie vào phía Genre
            genre.getMovies().add(this);
        }
    }

    public void removeGenre(Genre genre) {
        if (genre != null) {
            this.genres.remove(genre);
            // Vì Genre có mappedBy, ta cũng cần xóa Movie khỏi phía Genre
            genre.getMovies().remove(this);
        }
    }

   
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Sử dụng instanceof để tương thích với Proxy của JPA
        if (!(o instanceof Movie movie)) return false;
        // Chỉ so sánh id nếu cả hai đều không null
        return id != null && id.equals(movie.id);
    }

    @Override
    public int hashCode() {
        // Sử dụng getClass() để đảm bảo tính nhất quán với equals khi có Proxy
        // Hoặc có thể dùng Objects.hash(id) nếu id luôn được đảm bảo
        return getClass().hashCode();
    }

    // --- toString() (Tùy chọn, hữu ích cho debug) ---
    @Override
    public String toString() {
        return "Movie{" +
               "id=" + id +
               ", title='" + title + '\'' +
              
               '}';
    }
}
