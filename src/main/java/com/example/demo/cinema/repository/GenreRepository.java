package com.example.demo.cinema.repository;

import com.example.demo.cinema.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional; // Import Optional

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    //Tìm Genre theo tên (hữu ích khi thêm phim)
    Optional<Genre> findByNameIgnoreCase(String name);
}
