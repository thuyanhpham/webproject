package com.example.demo.cinema.repository; // Thay thế bằng package của bạn

import com.example.demo.cinema.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime; // Vẫn cần cho query phức tạp hơn nếu có
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    // Tìm suất chiếu theo ID phim và ngày cụ thể, sắp xếp theo giờ bắt đầu
    // Giả định Entity Showtime có trường "showDate" (LocalDate) và "startTime" (LocalTime)
    List<Showtime> findByMovieIdAndShowDateOrderByStartTimeAsc(Long movieId, LocalDate showDate);

    // Tìm suất chiếu theo ID phòng và ngày cụ thể, sắp xếp theo giờ bắt đầu
    // Giả định Entity Showtime có trường "showDate" (LocalDate) và "startTime" (LocalTime)
    List<Showtime> findByRoomIdAndShowDateOrderByStartTimeAsc(Long roomId, LocalDate showDate);

    // Lấy các ngày duy nhất có suất chiếu của một bộ phim, sắp xếp tăng dần
    // Query trực tiếp trên trường showDate
    @Query("SELECT DISTINCT s.showDate FROM Showtime s WHERE s.movie.id = :movieId ORDER BY s.showDate ASC")
    List<LocalDate> findDistinctShowDatesByMovieId(@Param("movieId") Long movieId);

    // Tìm các ngày có suất chiếu cho một phim cụ thể, sắp xếp theo ngày tăng dần
    // Chỉ lấy các ngày từ hôm nay trở đi
    @Query("SELECT DISTINCT s.showDate FROM Showtime s WHERE s.movie.id = :movieId AND s.showDate >= CURRENT_DATE ORDER BY s.showDate ASC")
    List<LocalDate> findDistinctShowDatesByMovieIdAndDateAfterOrEqual(@Param("movieId") Long movieId);
    
    // Tìm các loại hình trải nghiệm (experience) có sẵn cho một phim vào một ngày cụ thể
    @Query("SELECT DISTINCT s.experience FROM Showtime s WHERE s.movie.id = :movieId AND s.showDate = :showDate ORDER BY s.experience ASC")
    List<String> findDistinctExperiencesByMovieIdAndShowDate(@Param("movieId") Long movieId, @Param("showDate") LocalDate showDate);
    
    // Tìm tất cả các suất chiếu cho một phim, vào một ngày cụ thể (có thể lọc theo experience)
    // Nếu experience là null hoặc rỗng, trả về tất cả các experience cho ngày đó.
    // Tải kèm thông tin Movie và Room để tránh LazyInitializationException trong view
    @Query("SELECT s FROM Showtime s " +
           "JOIN FETCH s.movie m " +
           "JOIN FETCH s.room r " + // Giả sử bạn có mối quan hệ với Room và muốn tải nó
           "WHERE s.movie.id = :movieId " +
           "AND s.showDate = :showDate " +
           "AND (:experience IS NULL OR :experience = '' OR s.experience = :experience) " +
           "ORDER BY s.startTime ASC")
    List<Showtime> findShowtimesByMovieIdAndShowDateAndExperience(
            @Param("movieId") Long movieId,
            @Param("showDate") LocalDate showDate,
            @Param("experience") String experience
    );
}