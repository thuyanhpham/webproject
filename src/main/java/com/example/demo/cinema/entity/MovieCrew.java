package com.example.demo.cinema.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "movie_crew") // Tên bảng trung gian trong CSDL
public class MovieCrew {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_member_id", nullable = false)
    private CrewMember crewMember;

    @Column(name = "job_title", nullable = false, length = 100) // Vai trò (Director, Producer,...)
    private String jobTitle;

    // Constructors
    public MovieCrew() {}

    public MovieCrew(Movie movie, CrewMember crewMember, String jobTitle) {
        this.movie = movie;
        this.crewMember = crewMember;
        this.jobTitle = jobTitle;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Movie getMovie() { return movie; }
    public void setMovie(Movie movie) { this.movie = movie; }
    public CrewMember getCrewMember() { return crewMember; }
    public void setCrewMember(CrewMember crewMember) { this.crewMember = crewMember; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    // equals() and hashCode() - quan trọng nếu dùng Set
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieCrew movieCrew = (MovieCrew) o;
        return Objects.equals(movie, movieCrew.movie) &&
               Objects.equals(crewMember, movieCrew.crewMember) &&
               Objects.equals(jobTitle, movieCrew.jobTitle); // Thêm jobTitle để phân biệt
    }

    @Override
    public int hashCode() {
        return Objects.hash(movie, crewMember, jobTitle);
    }
}