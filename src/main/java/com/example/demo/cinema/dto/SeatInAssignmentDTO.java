package com.example.demo.cinema.dto;

public class SeatInAssignmentDTO {

    private int seatNumber;
    private Long seatTypeId;
    
    public int getSeatNumber() {
        return seatNumber;
    }
    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }
    public Long getSeatTypeId() {
        return seatTypeId;
    }
    public void setSeatTypeId(Long seatTypeId) {
        this.seatTypeId = seatTypeId;
    }

}
