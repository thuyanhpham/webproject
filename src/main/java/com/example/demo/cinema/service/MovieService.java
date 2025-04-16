package com.example.demo.cinema.service;

import com.example.demo.cinema.entity.Movie;
import com.example.demo.cinema.entity.Showtime;
import com.example.demo.cinema.exception.ResourceNotFoundException; 
import com.example.demo.cinema.repository.MovieRepository;
import com.example.demo.cinema.repository.ShowtimeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 
import org.springframework.util.StringUtils; 

import java.time.LocalDate;
import java.util.List;
import java.util.Optional; 

@Service 
public class MovieService {

    private final MovieRepository movieRepository;
  

    @Autowired 
    public MovieService(MovieRepository movieRepository /*, ReviewRepository reviewRepository */) {
        this.movieRepository = movieRepository;
        // this.reviewRepository = reviewRepository;
    }
    @Autowired private ShowtimeRepository showtimeRepository;
    
    /**
     * Tìm kiếm và phân trang phim cho trang danh sách (ví dụ: /movielist).
     * Sử dụng Transactional read-only để tối ưu.
     *
     * @param pageable Đối tượng chứa thông tin phân trang và sắp xếp.
     * @param query Từ khóa tìm kiếm (ví dụ: theo tiêu đề). Có thể null hoặc rỗng.
     * @return Page<Movie> chứa danh sách phim phù hợp và thông tin phân trang.
     */
    @Transactional(readOnly = true) 
    public Page<Movie> findMovies(Pageable pageable, String query) {
        
        if (StringUtils.hasText(query)) {
            
            return movieRepository.findByTitleContainingIgnoreCase(query, pageable);
        } else {
            
            return movieRepository.findAll(pageable);
        }
        // Lưu ý: Phương thức này không cần JOIN FETCH tất cả chi tiết,
        // vì trang danh sách thường chỉ hiển thị thông tin cơ bản.
    }

    /**
     * Tìm phim theo ID (Lấy thông tin cơ bản).
     * Phương thức này **không** đảm bảo load các collection LAZY (genres, reviews, cast, crew,...).
     * **Không nên dùng** cho trang chi tiết phim nếu cần hiển thị các thông tin liên quan đó.
     *
     * @param id ID của phim cần tìm.
     * @return Optional<Movie> chứa Movie nếu tìm thấy, hoặc Optional.empty() nếu không.
     *         (Trả về Optional thay vì null là cách thực hành tốt hơn)
     */
    @Transactional(readOnly = true)
    public Optional<Movie> findMovieByIdOptional(Long id) {
        return movieRepository.findById(id);
        
    }

    /**
     * Tìm phim theo ID với đầy đủ chi tiết cho trang moviedetail.html.
     * Sử dụng phương thức repository với JOIN FETCH để lấy tất cả các collection
     * liên quan (genres, reviews & user, movieCasts & castMember, movieCrews & crewMember)
     * trong một query duy nhất, tránh N+1 query và LazyInitializationException.
     * Đảm bảo load cả các @ElementCollection (photoUrls, availableFormats).
     *
     * @param id ID của phim cần tìm.
     * @return Movie object với tất cả dữ liệu liên quan đã được load.
     * @throws ResourceNotFoundException nếu không tìm thấy phim với ID đã cho.
     */
    @Transactional(readOnly = true) 
    public Movie findMovieByIdWithDetails(Long id) {
        // Gọi phương thức repository với JOIN FETCH
        Movie movie = movieRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + id)); // Ném lỗi nếu không tìm thấy

        // Trigger loading cho các @ElementCollection (nếu chúng được map là LAZY)
        // Việc truy cập size() sẽ buộc Hibernate phải load dữ liệu trong transaction hiện tại.
        // Điều này đảm bảo dữ liệu sẵn sàng khi template Thymeleaf truy cập.
        if (movie.getPhotoUrls() != null) {
             movie.getPhotoUrls().size();
        }
       if (movie.getAvailableFormats() != null) {
           movie.getAvailableFormats().size();
       }
       // Các collection khác (reviews, movieCasts, movieCrews, genres) đã được load
       // bởi JOIN FETCH trong query repository `findByIdWithDetails`.

        return movie; // Trả về đối tượng Movie đã load đầy đủ
    }

    

    /**
     * Lưu một phim mới hoặc cập nhật phim đã có.
     * @param movie Đối tượng Movie cần lưu.
     * @return Movie đã được lưu (có thể có ID được gán hoặc updatedAt được cập nhật).
     */
    @Transactional // Giao dịch ghi (không phải readOnly)
    public Movie saveMovie(Movie movie) {
        // Có thể thêm logic validation trước khi lưu ở đây
        return movieRepository.save(movie);
    }

    /**
     * Xóa phim theo ID.
     * @param id ID của phim cần xóa.
     * @throws ResourceNotFoundException nếu không tìm thấy phim để xóa.
     */
    @Transactional // Giao dịch ghi
    public void deleteMovie(Long id) {
        // Kiểm tra xem phim có tồn tại không trước khi xóa
        if (!movieRepository.existsById(id)) {
            throw new ResourceNotFoundException("Movie not found with id: " + id + ". Cannot delete.");
        }
        movieRepository.deleteById(id);
        // Lưu ý: CascadeType.ALL và orphanRemoval=true trên các mối quan hệ
        // trong Movie entity sẽ đảm bảo các bản ghi liên quan (Review, MovieCast, MovieCrew)
        // cũng bị xóa theo khi Movie bị xóa.
    }
    
}
