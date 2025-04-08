package com.example.demo.cinema.repository;

import com.example.demo.cinema.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Thêm nếu cần query động phức tạp
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> { // Thêm JpaSpecificationExecutor nếu cần lọc nâng cao

   
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

}
