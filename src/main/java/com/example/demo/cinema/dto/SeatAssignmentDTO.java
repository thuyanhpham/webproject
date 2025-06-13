package com.example.demo.cinema.dto;
 import java.util.List;
public class SeatAssignmentDTO {

    private String rowIdentifier;
    private List<SeatInAssignmentDTO> seats;

    public String getRowIdentifier() {
        return rowIdentifier;
    }
    public void setRowIdentifier(String rowIdentifier) {
        this.rowIdentifier = rowIdentifier;
    }
    public List<SeatInAssignmentDTO> getSeats() {
        return seats;
    }
    public void setSeats(List<SeatInAssignmentDTO> seats) {
        this.seats = seats;
    }

    
}
