package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
   

    @Autowired
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    /**
     * Tìm kiếm và phân trang phim.
     * @param pageable Đối tượng chứa thông tin phân trang và sắp xếp.
     * @param query Từ khóa tìm kiếm (ví dụ: theo tiêu đề).
     * @param filters Các bộ lọc khác (ví dụ: genreId, language...) - Cần thêm logic xử lý
     * @return Page chứa danh sách phim và thông tin phân trang.
     */
    public Page<Movie> findMovies(Pageable pageable, String query ) {
        
        if (StringUtils.hasText(query)) {
            return movieRepository.findByTitleContainingIgnoreCase(query, pageable);
        } else {
            return movieRepository.findAll(pageable);
        }
      
    }

    
    public Movie findMovieById(Long id) {
        return movieRepository.findById(id).orElse(null); 
    }
}
