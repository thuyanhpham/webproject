package com.example.demo.cinema.entity;

public enum MovieStatus {

	COMING_SOON("Coming Soon"),
	NOW_SHOWING("Now Showing");
	
	private final String displayName;
	
	MovieStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public String getName() {
		return name();
	}
}
