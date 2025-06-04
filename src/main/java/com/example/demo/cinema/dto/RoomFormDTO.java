package com.example.demo.cinema.dto;

import java.util.ArrayList;
import java.util.List;

public class RoomFormDTO {

	private Long id;
	private String name;
	private transient Integer capacity = 0;
	
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
	
}
