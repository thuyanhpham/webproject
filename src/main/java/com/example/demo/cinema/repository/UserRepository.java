package com.example.demo.cinema.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.cinema.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);
	Optional<User> findByEmail(String email);
	Optional<User> findByEmailIgnoreCase(String email);
	
	Boolean existsByUsername(String username);
	Boolean existsByEmail(String email);
}
