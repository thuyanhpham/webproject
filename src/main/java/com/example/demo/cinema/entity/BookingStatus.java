package com.example.demo.cinema.entity;

public enum BookingStatus {

	PENDING("Pending Payment"),
	CONFIRMED("Confirmed"),
	CANCELLED("Cancelled"),
	USED("Used");
	
	private final String displayName;
	BookingStatus(String displayName) {
		this.displayName = displayName;
	}
	public String getDisplayName() {
		return displayName;
	}
}
