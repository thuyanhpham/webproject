package com.example.demo.cinema.dto;

import java.math.BigDecimal;

public class SeatDisplayDTO {

    private Long seatId;
    private String seatLabel;
    private BigDecimal price;
    private SeatStatus status;
    private String typeName;
    private String color;
    private boolean isCouple;
    private String couplePart;

    public Long getSeatId() {
        return seatId;
    }
    public void setSeatId(Long seatId) {
        this.seatId = seatId;
    }
    public String getSeatLabel() {
        return seatLabel;
    }
    public void setSeatLabel(String seatLabel) {
        this.seatLabel = seatLabel;
    }
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public SeatStatus getStatus() {
        return status;
    }
    public void setStatus(SeatStatus status) {
        this.status = status;
    }
    public String getTypeName() {
        return typeName;
    }
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public boolean isCouple() {
        return isCouple;
    }
    public void setCouple(boolean isCouple) {
        this.isCouple = isCouple;
    }
    public String getCouplePart() {
        return couplePart;
    }
    public void setCouplePart(String couplePart) {
        this.couplePart = couplePart;
    }
}
