package com.example.demo.cinema.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "movie_cast") // Tên bảng trung gian trong CSDL
public class MovieCast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Hoặc dùng @EmbeddedId nếu muốn khóa chính phức hợp

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cast_member_id", nullable = false)
    private CastMember castMember;

    @Column(name = "character_name", nullable = false, length = 255) // Tên vai diễn
    private String characterName;

    // Constructors
    public MovieCast() {}

    public MovieCast(Movie movie, CastMember castMember, String characterName) {
        this.movie = movie;
        this.castMember = castMember;
        this.characterName = characterName;
    }

    // Getters and Setters
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

	public CastMember getCastMember() {
		return castMember;
	}

	public void setCastMember(CastMember castMember) {
		this.castMember = castMember;
	}

	public String getCharacterName() {
		return characterName;
	}

	public void setCharacterName(String characterName) {
		this.characterName = characterName;
	}


    // equals() and hashCode() - quan trọng nếu dùng Set
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieCast movieCast = (MovieCast) o;
        // So sánh dựa trên movie và castMember (hoặc id nếu dùng id đơn)
        return Objects.equals(movie, movieCast.movie) &&
               Objects.equals(castMember, movieCast.castMember) &&
               Objects.equals(characterName, movieCast.characterName); // Thêm cả characterName để phân biệt vai
    }


	@Override
    public int hashCode() {
        return Objects.hash(movie, castMember, characterName);
    }
}