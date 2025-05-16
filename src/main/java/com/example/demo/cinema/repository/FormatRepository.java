package com.example.demo.cinema.repository;

import com.example.demo.cinema.entity.Format;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FormatRepository extends JpaRepository<Format, Long> {
    Optional<Format> findByNameIgnoreCase(String name);
    List<Format> findByNameIn(List<String> names);
    Optional<Format> findById(Long id);
}
