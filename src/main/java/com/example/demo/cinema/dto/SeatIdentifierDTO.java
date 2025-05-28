package com.example.demo.cinema.dto;

public class SeatIdentifierDTO {
    private Long seatId;

    public SeatIdentifierDTO() {
    }

    public SeatIdentifierDTO(Long seatId) {
        this.seatId = seatId;
    }

    public Long getSeatId() {
        return seatId;
    }

    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }
}
