package com.example.demo.cinema.repository;

import com.example.demo.cinema.entity.CrewMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrewMemberRepository extends JpaRepository<CrewMember, Long> {
    // Có thể thêm phương thức tìm kiếm theo tên
    // List<CrewMember> findByNameContainingIgnoreCase(String name);
}