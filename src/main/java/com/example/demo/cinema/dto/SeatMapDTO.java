package com.example.demo.cinema.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class SeatMapDTO {

	private Long showtimeId;
	private Long movieId;
	private String movieTitle;
	private String movieBannerUrl;
	private String showDateForUrl;
	private String showFullDateDisplay;
	private String showStartTimeDisplay;
	private String roomName;
	private Integer roomCapacity;
	private List<SeatDTO> seats;
	private Map<String, List<SeatDTO>> groupedSeatsByRow;
	private BigDecimal defaultShowtimePrice;

	public Long getShowtimeId() {
		return showtimeId;
	}
	public void setShowtimeId(Long showtimeId) {
		this.showtimeId = showtimeId;
	}
	public Long getMovieId() {
		return movieId;
	}
	public void setMovieId(Long movieId) {
		this.movieId = movieId;
	}
	public String getMovieTitle() {
		return movieTitle;
	}
	public void setMovieTitle(String movieTitle) {
		this.movieTitle = movieTitle;
	}
	public String getMovieBannerUrl() {
		return movieBannerUrl;
	}
	public void setMovieBannerUrl(String movieBannerUrl) {
		this.movieBannerUrl = movieBannerUrl;
	}
	public String getShowDateForUrl() {
		return showDateForUrl;
	}
	public void setShowDateForUrl(String showDateForUrl) {
		this.showDateForUrl = showDateForUrl;
	}
	public String getShowFullDateDisplay() {
		return showFullDateDisplay;
	}
	public void setShowFullDateDisplay(String showFullDateDisplay) {
		this.showFullDateDisplay = showFullDateDisplay;
	}
	public String getShowStartTimeDisplay() {
		return showStartTimeDisplay;
	}
	public void setShowStartTimeDisplay(String showStartTimeDisplay) {
		this.showStartTimeDisplay = showStartTimeDisplay;
	}
	public String getRoomName() {
		return roomName;
	}
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}
	public Integer getRoomCapacity() {
		return roomCapacity;
	}
	public void setRoomCapacity(Integer roomCapacity) {
		this.roomCapacity = roomCapacity;
	}
	public List<SeatDTO> getSeats() {
		return seats;
	}
	public void setSeats(List<SeatDTO> seats) {
		this.seats = seats;
	}
	public Map<String, List<SeatDTO>> getGroupedSeatsByRow() {
		return groupedSeatsByRow;
	}
	public void setGroupedSeatsByRow(Map<String, List<SeatDTO>> groupedSeatsByRow) {
		this.groupedSeatsByRow = groupedSeatsByRow;
	}
	public BigDecimal getDefaultShowtimePrice() {
		return defaultShowtimePrice;
	}
	public void setDefaultShowtimePrice(BigDecimal defaultShowtimePrice) {
		this.defaultShowtimePrice = defaultShowtimePrice;
	}
}
