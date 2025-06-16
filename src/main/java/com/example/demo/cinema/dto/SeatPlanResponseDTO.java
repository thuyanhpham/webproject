package com.example.demo.cinema.dto;

import java.util.List;
import java.util.Map;

import com.example.demo.cinema.entity.Showtime;

public class SeatPlanResponseDTO {

    private Showtime showtime;
    private Map<String, List<SeatDisplayDTO>> seatLayout;
    private List<SeatTypeDTO> seatTypesLengend;

    public SeatPlanResponseDTO(Showtime showtime, Map<String, List<SeatDisplayDTO>> seatLayout, List<SeatTypeDTO> seatTypesLegend) {
        this.showtime = showtime;
        this.seatLayout = seatLayout;
        this.seatTypesLengend = seatTypesLegend;
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public void setShowtime(Showtime showtime) {
        this.showtime = showtime;
    }

    public Map<String, List<SeatDisplayDTO>> getSeatLayout() {
        return seatLayout;
    }

    public void setSeatLayout(Map<String, List<SeatDisplayDTO>> seatLayout) {
        this.seatLayout = seatLayout;
    }

    public List<SeatTypeDTO> getSeatTypesLengend() {
        return seatTypesLengend;
    }

    public void setSeatTypesLengend(List<SeatTypeDTO> seatTypesLengend) {
        this.seatTypesLengend = seatTypesLengend;
    }
}
