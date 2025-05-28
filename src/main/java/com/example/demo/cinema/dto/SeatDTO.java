package com.example.demo.cinema.dto;

import java.math.BigDecimal;

public class SeatDTO {

	private Long seatId;
	private String rowIdentifier;
	private Integer seatNumber;
	private String seatType;
	private String status;
	private boolean isPhysicallyAvailable;
	private BigDecimal price;
	public Long getSeatId() {
		return seatId;
	}
	public void setSeatId(Long seatId) {
		this.seatId = seatId;
	}
	public String getRowIdentifier() {
		return rowIdentifier;
	}
	public void setRowIdentifier(String rowIdentifier) {
		this.rowIdentifier = rowIdentifier;
	}
	public Integer getSeatNumber() {
		return seatNumber;
	}
	public void setSeatNumber(Integer seatNumber) {
		this.seatNumber = seatNumber;
	}
	public String getSeatType() {
		return seatType;
	}
	public void setSeatType(String seatType) {
		this.seatType = seatType;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public boolean isPhysicallyAvailable() {
		return isPhysicallyAvailable;
	}
	public void setPhysicallyAvailable(boolean isPhysicallyAvailable) {
		this.isPhysicallyAvailable = isPhysicallyAvailable;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	} 
}
