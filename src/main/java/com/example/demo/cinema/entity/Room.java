package com.example.demo.cinema.entity; // Hoặc package của bạn

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "rooms") // Tên bảng trong database
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

    @Column(name = "is_active", nullable = false) // Thường đặt tên cột là is_active hoặc active
    private boolean isActive = true; // Trạng thái phòng: true = hoạt động, false = không hoạt động

    // Constructors
    public Room() {
    }

    // Constructor này nên bao gồm cả trạng thái active nếu cần thiết
    public Room(String name, Integer capacity, boolean isActive) {
        this.name = name;
        this.capacity = capacity;
        this.isActive = isActive;
    }

    // Getters and Setters (Đã chuẩn)
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

	// Cẩn thận khi dùng setter cho collection quản lý bởi JPA
	// Thường dùng các phương thức add/remove thay thế
	public void setShowtimes(Set<Showtime> showtimes) {
		// Nên xóa các showtime cũ trước khi gán mới để đảm bảo orphanRemoval hoạt động đúng
        // if (this.showtimes != null) {
        //     this.showtimes.forEach(s -> s.setRoom(null)); // Ngắt liên kết hai chiều
        //     this.showtimes.clear();
        // }
        // if (showtimes != null) {
        //     showtimes.forEach(s -> s.setRoom(this)); // Thiết lập liên kết hai chiều
        //     this.showtimes.addAll(showtimes);
        // }
        this.showtimes = showtimes; // Cách đơn giản, hoạt động nếu cascade đúng
	}

	public boolean isActive() {
		return isActive;
	}
	// Đổi tên phương thức getter cho boolean theo chuẩn Java (is...)
	public boolean getIsActive() {
	    return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

    // ----- equals() and hashCode() (Đã chuẩn) -----
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room room)) return false;
        // Chỉ so sánh ID nếu cả hai đều không null, nếu không chúng không bằng nhau
        return id != null && Objects.equals(id, room.id);
    }

    @Override
    public int hashCode() {
        // Dùng getClass() là an toàn nhất khi dùng với Hibernate/JPA
        return getClass().hashCode();
    }

    // ----- toString() (Đã ổn) -----
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