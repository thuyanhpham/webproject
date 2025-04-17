package com.example.demo.cinema.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "cast_members")
public class CastMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name; // Tên diễn viên

    @Column(name = "image_url", length = 255) // URL ảnh đại diện
    private String imageUrl;

    // Mối quan hệ OneToMany đến bảng trung gian MovieCast
    // mappedBy trỏ đến tên field "castMember" trong entity MovieCast
    @OneToMany(mappedBy = "castMember", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<MovieCast> movieCasts = new HashSet<>();

    // Constructors
    public CastMember() {}

    public CastMember(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
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

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public Set<MovieCast> getMovieCasts() {
		return movieCasts;
	}

	public void setMovieCasts(Set<MovieCast> movieCasts) {
		this.movieCasts = movieCasts;
	}
    
    // equals() and hashCode() based on id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CastMember that = (CastMember) o;
        return id != null && Objects.equals(id, that.id);
    }



	@Override
    public int hashCode() {
        return getClass().hashCode();
    }
}