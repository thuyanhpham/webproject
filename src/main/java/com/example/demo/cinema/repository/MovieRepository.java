package com.example.demo.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.entity.MovieStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

	List<Movie> findByStatusAndReleaseDateLessThanEqualOrderByReleaseDateDesc(MovieStatus status, LocalDate today);

	List<Movie> findByStatusAndReleaseDateGreaterThanOrderByReleaseDateAsc(MovieStatus status, LocalDate today);

	void deleteMovieById(@Param("movieId") Long movieId);
	
	@Modifying
	@Query(value = "DELETE FROM movie_formats WHERE movie_id = :movieId", nativeQuery = true)
	void deleteFormatsByMovieIdNative(@Param("movieId") Long movieId);
	
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("SELECT m FROM Movie m " +
           "LEFT JOIN FETCH m.genres g " +
           "LEFT JOIN FETCH m.reviews r LEFT JOIN FETCH r.user ru " +
           "WHERE m.id = :id")
    Optional<Movie> findByIdWithDetails(@Param("id") Long id);
    
    @Query("SELECT DISTINCT m FROM Movie m JOIN m.showtimes s WHERE s.showDate = :searchDate")
    Page<Movie> findMoviesByShowDate(@Param("searchDate") LocalDate searchDate, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.releaseDate <= :today AND m.status = :status ORDER BY m.releaseDate DESC")
    List<Movie> findNowShowingMoviesByStatus(@Param("today") LocalDate today, @Param("status") MovieStatus status);
}