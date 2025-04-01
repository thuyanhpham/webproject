package com.example.demo.cinema.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.cinema.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	boolean existByUsername(String username);
	boolean existByEmail(String email);

	Optional<User> findByUsernameOrEmail(String username, String email);
	Optional<User> findByEmail(String email);
}
