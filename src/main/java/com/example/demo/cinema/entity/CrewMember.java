package com.example.demo.cinema.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "crew_members")
public class CrewMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name; // Tên thành viên đoàn

    @Column(name = "image_url", length = 255) // URL ảnh
    private String imageUrl;

    // Mối quan hệ OneToMany đến bảng trung gian MovieCrew
    @OneToMany(mappedBy = "crewMember", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<MovieCrew> movieCrews = new HashSet<>();

    // Constructors
    public CrewMember() {
    }

    public CrewMember(String name, String imageUrl) {
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

    public Set<MovieCrew> getMovieCrews() {
        return movieCrews;
    }

    public void setMovieCrews(Set<MovieCrew> movieCrews) {
        this.movieCrews = movieCrews;
    }

    // equals() and hashCode() based on id
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CrewMember that = (CrewMember) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}