package com.example.demo.cinema.entity;

public enum SeatType {

	NORMAL("Normal"),
	VIP("VIP"),
	COUPLE("couple Seat"),
	WHEELCHAIR("wheelchair Accessible");
	
	private final String displayName;
	SeatType(String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}
}
