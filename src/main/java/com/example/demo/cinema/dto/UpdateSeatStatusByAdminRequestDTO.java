package com.example.demo.cinema.dto;

import java.util.List;

public class UpdateSeatStatusByAdminRequestDTO {
    private List<SeatIdentifierDTO> seatsToUpdate;
    private String newAdminStatus;

    public List<SeatIdentifierDTO> getSeatsToUpdate() {
        return seatsToUpdate;
    }

    public void setSeatsToUpdate(List<SeatIdentifierDTO> seatsToUpdate) {
        this.seatsToUpdate = seatsToUpdate;
    }

    public String getNewAdminStatus() {
        return newAdminStatus;
    }

    public void setNewAdminStatus(String newAdminStatus) {
        this.newAdminStatus = newAdminStatus;
    }
}
