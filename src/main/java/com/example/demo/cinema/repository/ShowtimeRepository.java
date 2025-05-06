package com.example.demo.cinema.repository;

import com.example.demo.cinema.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    // --- Các phương thức hiện có (Giữ nguyên) ---

    // Tìm tất cả suất chiếu của một phim vào một ngày cụ thể, sắp xếp theo giờ bắt đầu
    List<Showtime> findByMovieIdAndShowDateOrderByStartTimeAsc(Long movieId, LocalDate showDate);

    // Tìm suất chiếu theo phim, ngày và định dạng (experience), sắp xếp theo giờ bắt đầu
    List<Showtime> findByMovieIdAndShowDateAndExperienceIgnoreCaseOrderByStartTimeAsc(Long movieId, LocalDate showDate, String experience);

    // Lấy danh sách các ngày có suất chiếu cho một phim (không trùng lặp) từ ngày hiện tại trở đi
    @Query("SELECT DISTINCT s.showDate FROM Showtime s WHERE s.movie.id = :movieId AND s.showDate >= CURRENT_DATE ORDER BY s.showDate ASC")
    List<LocalDate> findDistinctShowDatesByMovieId(@Param("movieId") Long movieId);

    // Lấy danh sách các định dạng (experience) có suất chiếu cho phim vào một ngày cụ thể (không trùng lặp)
    @Query("SELECT DISTINCT s.experience FROM Showtime s WHERE s.movie.id = :movieId AND s.showDate = :showDate ORDER BY s.experience ASC")
    List<String> findDistinctExperiencesByMovieIdAndShowDate(@Param("movieId") Long movieId, @Param("showDate") LocalDate showDate);


    // --- PHƯƠNG THỨC MỚI CẦN THÊM cho HomeController (thông qua ShowtimeService) ---

    /**
     * Lấy tất cả các ngày chiếu duy nhất trong hệ thống từ một ngày cụ thể trở về sau.
     * Dùng cho dropdown ngày chung trên trang chủ/trang danh sách.
     * @param date Ngày bắt đầu tìm kiếm (ví dụ: LocalDate.now()).
     * @return Danh sách các ngày duy nhất.
     */
    @Query("SELECT DISTINCT s.showDate FROM Showtime s WHERE s.showDate >= :date ORDER BY s.showDate ASC")
    List<LocalDate> findDistinctShowDatesAfter(@Param("date") LocalDate date);

}