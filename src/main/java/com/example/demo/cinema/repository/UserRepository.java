package com.example.demo.cinema.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.cinema.entity.Status;
import com.example.demo.cinema.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	@Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.username = :username")
    Optional<User> findByUsernameWithRole(@Param("username") String username);
	Optional<User> findByUsername(String username);
	Optional<User> findByEmail(String email);
	Optional<User> findByEmailIgnoreCase(String email);
	List<User> findByStatusNot(Status status);
}
