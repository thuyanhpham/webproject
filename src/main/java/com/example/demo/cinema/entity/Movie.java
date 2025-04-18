package com.example.demo.cinema.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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

    @Column(nullable = false, length = 100)
    private String director;

//    @Lob
//    @Column(nullable = false, columnDefinition = "TEXT")
//    private String actors; // Lưu ý: Trường này có thể không còn cần thiết nếu dùng MovieCast

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "trailer_url", nullable = false, length = 255)
    private String trailerUrl;

    @Column(name = "poster_url", nullable = false, length = 255)
    private String posterUrl;

    @Column(name = "banner_url", length = 255) // Đã thêm bannerUrl
    private String bannerUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Showtime> showtimes = new HashSet<>();

    // ... các getter/setter khác ...

    public Set<Showtime> getShowtimes() {
        return showtimes;
    }

    public void setShowtimes(Set<Showtime> showtimes) {
        this.showtimes = showtimes;
    }

    // (Tùy chọn) Helper method
    public void addShowtime(Showtime showtime) {
        if (showtime != null) {
            this.showtimes.add(showtime);
            showtime.setMovie(this); // Thiết lập mối quan hệ hai chiều
        }
    }

    public void removeShowtime(Showtime showtime) {
         if (showtime != null) {
            this.showtimes.remove(showtime);
            showtime.setMovie(null);
        }
    } 
    // Mối quan hệ với Genre
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "movie_genres",
        joinColumns = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

    // Mối quan hệ với Review
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Review> reviews = new HashSet<>();

    // Mối quan hệ với MovieCast (bảng trung gian cast)
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<MovieCast> movieCasts = new HashSet<>();

    // Mối quan hệ với MovieCrew (bảng trung gian crew)
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<MovieCrew> movieCrews = new HashSet<>();

    // Danh sách URL ảnh chi tiết
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "movie_photos", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "photo_url", nullable = false, length=255)
    private List<String> photoUrls = new ArrayList<>();

    // Các định dạng phim có sẵn
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "movie_formats", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "format_name", nullable = false, length=50)
    private Set<String> availableFormats = new HashSet<>();


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructor mặc định
    public Movie() {}

    // --- Getters and Setters cho tất cả các trường ---
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
//    public String getActors() { return actors; } // Giữ lại nếu vẫn dùng, nhưng nên ưu tiên movieCasts
//    public void setActors(String actors) { this.actors = actors; }
    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Set<Genre> getGenres() { return genres; }
    public void setGenres(Set<Genre> genres) { this.genres = genres; }
    public Set<Review> getReviews() { return reviews; }
    public void setReviews(Set<Review> reviews) { this.reviews = reviews; }
    public Set<MovieCast> getMovieCasts() { return movieCasts; }
    public void setMovieCasts(Set<MovieCast> movieCasts) { this.movieCasts = movieCasts; }
    public Set<MovieCrew> getMovieCrews() { return movieCrews; }
    public void setMovieCrews(Set<MovieCrew> movieCrews) { this.movieCrews = movieCrews; }
    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }
    public Set<String> getAvailableFormats() { return availableFormats; }
    public void setAvailableFormats(Set<String> availableFormats) { this.availableFormats = availableFormats; }

    // --- Helper methods ---
    public void addGenre(Genre genre) {
        if (genre != null) {
            this.genres.add(genre);
            genre.getMovies().add(this);
        }
    }

    public void removeGenre(Genre genre) {
        if (genre != null) {
            this.genres.remove(genre);
            genre.getMovies().remove(this);
        }
    }

    public void addCastMember(CastMember castMember, String characterName) {
        MovieCast movieCast = new MovieCast(this, castMember, characterName);
        this.movieCasts.add(movieCast);
    }

    public void removeCastMember(CastMember castMember) {
        // Sửa lại: dùng equals chuẩn
        this.movieCasts.removeIf(mc -> mc.getMovie().equals(this) && mc.getCastMember().equals(castMember));
    }

    public void addCrewMember(CrewMember crewMember, String jobTitle) {
         MovieCrew movieCrew = new MovieCrew(this, crewMember, jobTitle);
         this.movieCrews.add(movieCrew);
    }

    public void removeCrewMember(CrewMember crewMember) {
        // Sửa lại: dùng equals chuẩn
        this.movieCrews.removeIf(mc -> mc.getMovie().equals(this) && mc.getCrewMember().equals(crewMember));
    }

    // --- equals() và hashCode() chuẩn ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Movie movie)) return false;
        return id != null && id.equals(movie.id);
    }

    @Override
    public int hashCode() {
        // Dùng getClass().hashCode() an toàn với proxy của JPA
        return getClass().hashCode();
        // Hoặc nếu id luôn được gán trước khi vào Set/Map: return Objects.hash(id);
    }

    // --- toString() ---
    @Override
    public String toString() {
        return "Movie{" +
               "id=" + id +
               ", title='" + title + '\'' +
               '}';
    }
}
