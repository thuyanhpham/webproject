package com.example.demo.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Import @Query
import org.springframework.data.repository.query.Param; // Import @Param
import org.springframework.stereotype.Repository;

import com.example.demo.cinema.entity.Movie;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional; // Import Optional

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // --- Các phương thức cần cho HomeController và MovieService ---

    List<Movie> findByReleaseDateLessThanEqualOrderByReleaseDateDesc(LocalDate today);

    List<Movie> findByReleaseDateGreaterThanOrderByReleaseDateAsc(LocalDate today);

    // --- Phương thức cần cho MovieController ---

    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    // findAll(Pageable pageable) đã có sẵn

    // --- Phương thức cần cho findMovieByIdWithDetails trong MovieService ---
    /**
     * Tìm phim theo ID và JOIN FETCH các collection liên quan để tránh LazyInitializationException.
     * Bao gồm genres, movieCasts (với castMember), movieCrews (với crewMember).
     * Không cần fetch reviews ở đây nếu bạn muốn lấy chúng riêng khi cần.
     * @param id ID của phim
     * @return Optional<Movie> với các collection đã được load.
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
           "LEFT JOIN FETCH m.genres g " + // Fetch genres
           "LEFT JOIN FETCH m.movieCasts mc LEFT JOIN FETCH mc.castMember cm " + // Fetch cast
           "LEFT JOIN FETCH m.movieCrews mcr LEFT JOIN FETCH mcr.crewMember crm " + // Fetch crew
           // "LEFT JOIN FETCH m.reviews r LEFT JOIN FETCH r.user u " + // Fetch reviews và user (TÙY CHỌN - có thể làm nặng query)
           "WHERE m.id = :id")
    Optional<Movie> findByIdWithDetails(@Param("id") Long id); // Trả về Optional

   
}