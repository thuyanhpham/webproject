package com.example.demo.cinema.repository;

import com.example.demo.cinema.entity.Movie;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Thêm nếu cần query động phức tạp
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> { 

   
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    // --- PHƯƠNG THỨC MỚI ĐỂ LẤY CHI TIẾT PHIM ---
    // Sử dụng LEFT JOIN FETCH để lấy các collection liên quan trong 1 query
    @Query("SELECT m FROM Movie m " +
           "LEFT JOIN FETCH m.genres g " + // Lấy genres
           "LEFT JOIN FETCH m.reviews r LEFT JOIN FETCH r.user ru " + // Lấy reviews và user của review đó
           "LEFT JOIN FETCH m.movieCasts mc LEFT JOIN FETCH mc.castMember cm " + // Lấy movieCasts và castMember
           "LEFT JOIN FETCH m.movieCrews mcr LEFT JOIN FETCH mcr.crewMember crm " + // Lấy movieCrews và crewMember
           "WHERE m.id = :id")
    Optional<Movie> findByIdWithDetails(@Param("id") Long id);

    // Lưu ý: @ElementCollection (photoUrls, availableFormats) thường được fetch LAZY
    // và có thể cần truy cập trong transaction hoặc fetch riêng nếu cần tối ưu đặc biệt.
    // Tuy nhiên, với JOIN FETCH ở trên, các quan hệ chính đã được xử lý.

}
