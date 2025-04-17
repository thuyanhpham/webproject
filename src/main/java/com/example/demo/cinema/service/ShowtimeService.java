package com.example.demo.cinema.service; // Thay đổi package nếu cần

import com.example.demo.cinema.entity.Movie; // Import Movie entity
import com.example.demo.cinema.entity.Showtime; // Import Showtime entity
import com.example.demo.cinema.exception.ResourceNotFoundException; // Import Exception
import com.example.demo.cinema.repository.MovieRepository; // Import Movie Repo
import com.example.demo.cinema.repository.ShowtimeRepository; // Import Showtime Repo

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional
import org.springframework.util.StringUtils; // Import StringUtils để kiểm tra chuỗi rỗng

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service 
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository; 

    @Autowired // Constructor Injection
    public ShowtimeService(ShowtimeRepository showtimeRepository, MovieRepository movieRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
    }

    /**
     * Tìm các suất chiếu khả dụng cho một phim, vào một ngày cụ thể,
     * và tùy chọn lọc theo định dạng (experience).
     * Sắp xếp theo thời gian bắt đầu.
     *
     * @param movieId     ID của phim.
     * @param showDate    Ngày chiếu cần tìm. Nếu null, mặc định là ngày hiện tại.
     * @param experience  Định dạng chiếu (ví dụ: "2D", "IMAX"). Nếu null hoặc rỗng, không lọc theo định dạng.
     * @return Danh sách các Suất chiếu phù hợp.
     * @throws ResourceNotFoundException nếu không tìm thấy phim với movieId.
     */
    @Transactional(readOnly = true)
    public List<Showtime> findAvailableShowtimes(Long movieId, LocalDate showDate, String experience) {
        // 1. Kiểm tra xem phim có tồn tại không
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Cannot find showtimes for non-existent Movie with id: " + movieId);
        }

        // 2. Xác định ngày lọc (mặc định là hôm nay)
        LocalDate dateToFilter = (showDate != null) ? showDate : LocalDate.now();

        // 3. Gọi phương thức repository phù hợp dựa trên có lọc experience hay không
        if (StringUtils.hasText(experience)) {
            // Có lọc experience
            return showtimeRepository.findByMovieIdAndShowDateAndExperienceIgnoreCaseOrderByStartTimeAsc(movieId, dateToFilter, experience);
        } else {
            // Không lọc experience, chỉ lọc theo ngày
            return showtimeRepository.findByMovieIdAndShowDateOrderByStartTimeAsc(movieId, dateToFilter);
        }
    }

    /**
     * Lấy danh sách các ngày (không trùng lặp) có suất chiếu cho một phim cụ thể,
     * tính từ ngày hiện tại trở đi.
     *
     * @param movieId ID của phim.
     * @return Danh sách các LocalDate.
     * @throws ResourceNotFoundException nếu không tìm thấy phim với movieId.
     */
    @Transactional(readOnly = true)
    public List<LocalDate> findAvailableDatesForMovie(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Cannot find dates for non-existent Movie with id: " + movieId);
        }
        // Gọi phương thức repository đã tạo để lấy ngày duy nhất
        return showtimeRepository.findDistinctShowDatesByMovieId(movieId);
    }

    /**
     * Lấy danh sách các định dạng (experience) (không trùng lặp) có suất chiếu
     * cho một phim cụ thể vào một ngày cụ thể.
     *
     * @param movieId   ID của phim.
     * @param showDate  Ngày chiếu cần kiểm tra. Nếu null, mặc định là ngày hiện tại.
     * @return Danh sách các chuỗi định dạng (experience).
     * @throws ResourceNotFoundException nếu không tìm thấy phim với movieId.
     */
    @Transactional(readOnly = true)
    public List<String> findAvailableExperiencesForMovieAndDate(Long movieId, LocalDate showDate) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Cannot find experiences for non-existent Movie with id: " + movieId);
        }

        // Xác định ngày lọc
        LocalDate dateToFilter = (showDate != null) ? showDate : LocalDate.now();

        // Gọi phương thức repository đã tạo
        return showtimeRepository.findDistinctExperiencesByMovieIdAndShowDate(movieId, dateToFilter);
    }

    /**
     * Tìm một suất chiếu cụ thể theo ID của nó.
     * (Hữu ích cho trang chọn ghế hoặc lấy chi tiết suất chiếu).
     *
     * @param showtimeId ID của suất chiếu cần tìm.
     * @return Đối tượng Showtime.
     * @throws ResourceNotFoundException nếu không tìm thấy suất chiếu.
     */
    @Transactional(readOnly = true)
    public Showtime findShowtimeById(Long showtimeId) {
        return showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + showtimeId));
    }

    // ----- Các phương thức khác có thể cần -----

    /**
     * Lưu một suất chiếu mới hoặc cập nhật suất chiếu đã có.
     * @param showtime Đối tượng Showtime cần lưu.
     * @return Showtime đã được lưu.
     */
    @Transactional // Giao dịch ghi dữ liệu
    public Showtime saveShowtime(Showtime showtime) {
        // Có thể thêm validation ở đây (ví dụ: kiểm tra movie có tồn tại, thời gian hợp lệ...)
        if (showtime.getMovie() == null || !movieRepository.existsById(showtime.getMovie().getId())) {
             throw new IllegalArgumentException("Showtime must be associated with an existing Movie.");
        }
        return showtimeRepository.save(showtime);
    }

    /**
     * Xóa một suất chiếu theo ID.
     * @param showtimeId ID của suất chiếu cần xóa.
     * @throws ResourceNotFoundException nếu không tìm thấy suất chiếu để xóa.
     */
    @Transactional // Giao dịch ghi dữ liệu
    public void deleteShowtime(Long showtimeId) {
        if (!showtimeRepository.existsById(showtimeId)) {
            throw new ResourceNotFoundException("Showtime not found with id: " + showtimeId + ". Cannot delete.");
        }
        showtimeRepository.deleteById(showtimeId);
    }
}