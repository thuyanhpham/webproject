package com.example.demo.cinema.entity; // Thay package nếu cần

import jakarta.persistence.*; // Hoặc javax...
// Import các annotation khác nếu cần

@Entity
@Table(name = "roles") // Đảm bảo tên bảng đúng
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Dùng IDENTITY cho MySQL Auto Increment
    private Long id;

    @Column(nullable = false, unique = true) // Tên role phải là duy nhất
    private String name;

    // Constructors, Getters, Setters, equals/hashCode (Dựa trên ID là tốt nhất)

    public Role() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return id != null && id.equals(role.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode(); // An toàn nhất cho JPA
    }
}