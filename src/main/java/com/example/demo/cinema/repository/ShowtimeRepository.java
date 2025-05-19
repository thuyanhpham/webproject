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
    @Query("SELECT s FROM Showtime s " +
           "JOIN FETCH s.movie m " +
           "JOIN FETCH s.room r " + 
           "WHERE s.movie.id = :movieId " +
           "AND s.showDate = :showDate " +
           "AND (:experience IS NULL OR :experience = '' OR s.experience = :experience) " +
           "ORDER BY s.startTime ASC")
    List<Showtime> findShowtimesByMovieIdAndShowDateAndExperience(@Param("movieId") Long movieId, @Param("showDate") LocalDate showDate, @Param("experience") String experience);
    @Modifying
    @Query("UPDATE Showtime s SET s.deleted = true WHERE " +
           "(s.showDate < :today OR (s.showDate = :today AND s.startTime < :time)) " +
           "AND s.deleted = false")
    int markAsDeletedOldShowtimes(@Param("today") LocalDate today, @Param("time") LocalTime time);
}