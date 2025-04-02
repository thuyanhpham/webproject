package com.example.demo.cinema.entity;

public enum Status {

	ACTIVE,
	INACTIVE,
	BANNED;
	
	private String value;
	
	public String getValue() {
		return value;
	}
	
	public static Status fromString(String status) {
		for (Status s : Status.values()) {
			if (s.getValue().equalsIgnoreCase(status)) {
				return s;
			}
		}
		throw new IllegalArgumentException("Unknown status: " + status);
	}
}
