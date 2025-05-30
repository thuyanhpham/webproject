package com.example.demo.cinema.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "seats")
public class Seat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String rowIdentifier;
	
	private boolean isActive = true;
	
	@ManyToOne
	private Room room;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seat_type_id", nullable = false)
	private SeatType seatType;
	
	@Column(nullable = false)
	private Integer seatNumber;
	
	@Column(length = 10)
	private String seatLabel;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRowIdentifier() {
		return rowIdentifier;
	}

	public void setRowIdentifier(String rowIdentifier) {
		this.rowIdentifier = rowIdentifier;
	}

	public SeatType getSeatType() {
		return seatType;
	}

	public void setSeatType(SeatType seatType) {
		this.seatType = seatType;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public Integer getSeatNumber() {
		return seatNumber;
	}

	public void setSeatNumber(Integer seatNumber) {
		this.seatNumber = seatNumber;
	}

	public String getSeatLabel() {
		return seatLabel;
	}

	public void setSeatLabel(String seatLabel) {
		this.seatLabel = seatLabel;
	}

}
