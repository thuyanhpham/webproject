package com.example.demo.cinema.dto;

public class RowDefinitionDTO {

	private Long id;
	private String identifier;
	private Integer numberOfSeats;
	private Integer order;
	private Long seatTypeId;
	
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public Integer getNumberOfSeats() {
		return numberOfSeats;
	}
	public void setNumberOfSeats(Integer numberOfSeats) {
		this.numberOfSeats = numberOfSeats;
	}
	public Integer getOrder() {
		return order;
	}
	public void setOrder(Integer order) {
		this.order = order;
	}
	public Long getSeatTypeId() {
		return seatTypeId;
	}
	public void setSeatTypeId(Long seatTypeId) {
		this.seatTypeId = seatTypeId;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
}
