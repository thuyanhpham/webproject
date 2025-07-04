package com.example.demo.cinema.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.Objects;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "showtimes")
@SQLDelete(sql = "UPDATE showtimes SET deleted = true, updated_at = NOW() WHERE id = ?")
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) // <-- TẠM THỜI THAY ĐỔI THÀNH EAGER
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(name = "show_date", nullable = false)
    private LocalDate showDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(nullable = false, length = 50)
    private String experience;
    
    @Column(precision = 10, scale = 2) 
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
            this.updatedAt = LocalDateTime.now();
    }

    public Showtime() { }

    public Showtime(Movie movie, LocalDate showDate, LocalTime startTime, String experience, Room room) {
        this.movie = movie;
        this.showDate = showDate;
        this.startTime = startTime;
        this.experience = experience;
        this.room = room;
    }

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Movie getMovie() {
		return movie;
	}

	public void setMovie(Movie movie) {
		this.movie = movie;
	}

	public LocalDate getShowDate() {
		return showDate;
	}

	public void setShowDate(LocalDate showDate) {
		this.showDate = showDate;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public String getExperience() {
		return experience;
	}

	public void setExperience(String experience) {
		this.experience = experience;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	// ----- equals() and hashCode() -----
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Showtime showtime)) return false;
        return id != null && Objects.equals(id, showtime.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Showtime{" +
               "id=" + id +
               ", movie=" + (movie != null ? movie.getTitle() : "null") +
               ", showDate=" + showDate +
               ", startTime=" + startTime +
               ", experience='" + experience + '\'' +
               ", deleted=" + deleted +
               ", room=" + (room != null ? room.getName() : "null") +
               '}';
    }

}
