package com.example.demo.cinema.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;


@Entity
@Table(name = "genres")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Mối quan hệ ngược lại (Optional nhưng hữu ích)
    // mappedBy trỏ đến tên field trong Movie entity chứa danh sách Genre
    @ManyToMany(mappedBy = "genres")
    private Set<Movie> movies = new HashSet<>();


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Genre() {}

    public Genre(String name) {
        this.name = name;
    }

    // Getters and Setters (cho id, name, createdAt, updatedAt, movies)
    public Long getId() {
 		return id;
 	}

 	public void setId(Long id) {
 		this.id = id;
 	}

 	public String getName() {
 		return name;
 	}

 	public void setName(String name) {
 		this.name = name;
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

 	public Set<Movie> getMovies() {
 		return movies;
 	}

 	public void setMovies(Set<Movie> movies) {
 		this.movies = movies;
 	}



    // equals() and hashCode() dựa trên 'id' hoặc 'name' (nếu unique) là quan trọng
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genre genre = (Genre) o;
        // Ưu tiên dùng ID nếu đã được gán, nếu không dùng name (giả sử name là unique)
        if (id != null) {
             return Objects.equals(id, genre.id);
        }
        return Objects.equals(name, genre.name);
    }

 
	@Override
    public int hashCode() {
         if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(name);
    }
}