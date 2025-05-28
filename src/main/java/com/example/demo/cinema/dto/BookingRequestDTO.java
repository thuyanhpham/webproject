package com.example.demo.cinema.dto;

import java.util.List;

public class BookingRequestDTO {
    private Long showtimeId;
    private List<Long> seatIds;
    
	public Long getShowtimeId() {
		return showtimeId;
	}
	public void setShowtimeId(Long showtimeId) {
		this.showtimeId = showtimeId;
	}
	public List<Long> getSeatIds() {
		return seatIds;
	}
	public void setSeatIds(List<Long> seatIds) {
		this.seatIds = seatIds;
	}

}
