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

    // Tìm tất cả suất chiếu của một phim vào một ngày cụ thể, sắp xếp theo giờ bắt đầu
    List<Showtime> findByMovieIdAndShowDateOrderByStartTimeAsc(Long movieId, LocalDate showDate);

    // Tìm suất chiếu theo phim, ngày và định dạng (experience), sắp xếp theo giờ bắt đầu
    List<Showtime> findByMovieIdAndShowDateAndExperienceIgnoreCaseOrderByStartTimeAsc(Long movieId, LocalDate showDate, String experience);

    // Lấy danh sách các ngày có suất chiếu cho một phim (không trùng lặp)
    @Query("SELECT DISTINCT s.showDate FROM Showtime s WHERE s.movie.id = :movieId AND s.showDate >= CURRENT_DATE ORDER BY s.showDate ASC")
    List<LocalDate> findDistinctShowDatesByMovieId(@Param("movieId") Long movieId);

    // Lấy danh sách các định dạng (experience) có suất chiếu cho phim vào một ngày cụ thể (không trùng lặp)
    @Query("SELECT DISTINCT s.experience FROM Showtime s WHERE s.movie.id = :movieId AND s.showDate = :showDate ORDER BY s.experience ASC")
    List<String> findDistinctExperiencesByMovieIdAndShowDate(@Param("movieId") Long movieId, @Param("showDate") LocalDate showDate);

}