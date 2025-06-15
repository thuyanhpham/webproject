package com.example.demo.cinema.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;

public class RoomFormDTO {

	private Long id;
	private String name;
	private transient Integer capacity = 0;
	private String seatAssignmentsJson;
    private boolean isActive = true; 
	private List<RowDefinitionDTO> rowDefinitions;
	
	public RoomFormDTO() {
		this.rowDefinitions = new ArrayList<>();
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
	public Integer getCapacity() {
		if (this.rowDefinitions != null) {
			return this.rowDefinitions.stream()
					.mapToInt(row -> row.getNumberOfSeats() == null ? 0 : row.getNumberOfSeats())
					.sum();
		}
		return 0;
	}

	public List<RowDefinitionDTO> getRowDefinitions() {
		return rowDefinitions;
	}

	public void setRowDefinitions(List<RowDefinitionDTO> rowDefinitions) {
		this.rowDefinitions = rowDefinitions;
	}

	public String getSeatAssignmentsJson() {
		return seatAssignmentsJson;
	}

	public void setSeatAssignmentsJson(String seatAssignmentsJson) {
		this.seatAssignmentsJson = seatAssignmentsJson;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
}
