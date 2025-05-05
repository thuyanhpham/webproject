package com.example.demo.cinema.repository;

import com.example.demo.cinema.entity.Movie;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Thêm nếu cần query động phức tạp
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> { 

	@Modifying
	@Query("DELETE FROM Movie m WHERE m.id = :movieId")
   
	void deleteMovieById(@Param("movieId") Long movieId);
	
	@Modifying
	@Query(value = "DELETE FROM movie_formats WHERE movie_id = :movieId", nativeQuery = true)
	void deleteFormatsByMovieIdNative(@Param("movieId") Long movieId);
	
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    // --- PHƯƠNG THỨC MỚI ĐỂ LẤY CHI TIẾT PHIM ---
    // Sử dụng LEFT JOIN FETCH để lấy các collection liên quan trong 1 query
    @Query("SELECT m FROM Movie m " +
           "LEFT JOIN FETCH m.genres g " + // Lấy genres
           "LEFT JOIN FETCH m.reviews r LEFT JOIN FETCH r.user ru " + // Lấy reviews và user của review đó
           "WHERE m.id = :id")
    Optional<Movie> findByIdWithDetails(@Param("id") Long id);

}