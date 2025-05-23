package com.example.demo.cinema.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    @OneToMany(mappedBy = "room", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Showtime> showtimes = new HashSet<>();

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public Room() {
    }

    public Room(String name, Integer capacity, boolean isActive) {
        this.name = name;
        this.capacity = capacity;
        this.isActive = isActive;
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
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	public Set<Showtime> getShowtimes() {
		return showtimes;
	}

	public void setShowtimes(Set<Showtime> showtimes) {
        this.showtimes = showtimes;
	}

	public boolean isActive() {
		return isActive;
	}

	public boolean getIsActive() {
	    return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room room)) return false;
        return id != null && Objects.equals(id, room.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Room{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", capacity=" + capacity +
               ", isActive=" + isActive +
               '}';
    }
}