package com.example.demo.cinema.dto;

import java.math.BigDecimal;

public class SeatTypeDTO {

    private Long id;
    private String name;
    private String color;
    private boolean isCouple;
    private BigDecimal price;
    
    public SeatTypeDTO() {
    }

    public SeatTypeDTO(Long id, String name, String color, boolean couple, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.isCouple = isCouple;
        this.price = price;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
    public BigDecimal getPrice() {
        return price;
    }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
