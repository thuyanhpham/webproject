  package com.example.demo.cinema.repository;

import com.example.demo.cinema.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    List<Showtime> findByMovieIdAndShowDateOrderByStartTimeAsc(Long movieId, LocalDate showDate);
    
    List<Showtime> findByRoomIdAndShowDateOrderByStartTimeAsc(Long roomId, LocalDate showDate);
    
    @Query("SELECT DISTINCT s.showDate FROM Showtime s WHERE s.movie.id = :movieId ORDER BY s.showDate ASC")
    List<LocalDate> findDistinctShowDatesByMovieId(@Param("movieId") Long movieId);
    
    @Query("SELECT DISTINCT s.showDate FROM Showtime s WHERE s.movie.id = :movieId AND s.showDate >= CURRENT_DATE ORDER BY s.showDate ASC")
    List<LocalDate> findDistinctShowDatesByMovieIdAndDateAfterOrEqual(@Param("movieId") Long movieId);
    
    @Query("SELECT DISTINCT s.experience FROM Showtime s WHERE s.movie.id = :movieId AND s.showDate = :showDate ORDER BY s.experience ASC")
    List<String> findDistinctExperiencesByMovieIdAndShowDate(@Param("movieId") Long movieId, @Param("showDate") LocalDate showDate);
    
    @Query("SELECT s FROM Showtime s " + "JOIN FETCH s.movie m " + "JOIN FETCH s.room r " + "WHERE s.movie.id = :movieId " + "AND s.showDate = :showDate " +
           "AND (:experience IS NULL OR :experience = '' OR s.experience = :experience) " + "ORDER BY s.startTime ASC")
    List<Showtime> findShowtimesByMovieIdAndShowDateAndExperience(@Param("movieId") Long movieId, @Param("showDate") LocalDate showDate, @Param("experience") String experience);
    
    @Query("SELECT s FROM Showtime s WHERE s.id = :id AND s.deleted = false")
    Optional<Showtime> findByIdAndNotDeleted(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Showtime s SET s.deleted = true, s.updatedAt = CURRENT_TIMESTAMP WHERE " + 
           "(s.showDate < :today OR (s.showDate = :today AND s.startTime < :time)) " +
           "AND s.deleted = false")
    int markOldShowtimesAsDeleted(@Param("today") LocalDate today, @Param("time") LocalTime time);
    
    @Query("SELECT sh FROM Showtime sh " +
            "JOIN FETCH sh.movie m " + // <--- Đảm bảo có "JOIN FETCH sh.movie m"
            "JOIN FETCH sh.room r " +  // <--- Đảm bảo có "JOIN FETCH sh.room r"
            // Nếu bạn muốn cả seats cũng được load: "LEFT JOIN FETCH r.seats s " +
            "WHERE sh.id = :showtimeId AND sh.deleted = false")
     Optional<Showtime> findByIdWithMovieAndRoomAndSeats(@Param("showtimeId") Long showtimeId);
}