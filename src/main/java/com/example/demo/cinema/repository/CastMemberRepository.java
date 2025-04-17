package com.example.demo.cinema.repository;

import com.example.demo.cinema.entity.CastMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CastMemberRepository extends JpaRepository<CastMember, Long> {
    // Có thể thêm phương thức tìm kiếm diễn viên theo tên nếu cần
    // List<CastMember> findByNameContainingIgnoreCase(String name);
}