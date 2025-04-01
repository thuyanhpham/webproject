package com.example.demo.cinema.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.repository.UserRepository;
@Service
public class UserService {
	
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	
	public void saveUser(User user) {
		userRepository.save(user);
	}
	
	public boolean existByUsername(String username) {
		return userRepository.existByUsername(username);
	}
	
	public boolean existByEmail(String email) {
		return userRepository.existByEmail(email);
	}
	
	public Optional<User> authenticateUser(String usernameOrEmail, String password) {
		Optional<User> user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
		
		if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
			return user;
		}
		
		return Optional.empty();
		
	}
	
	public void updatePasswordByEmail(String email, String newPassword) {
		Optional<User> userOpt = userRepository.findByEmail(email);
		if (userOpt.isPresent()) {
			User user = userOpt.get();
			user.setPassword(passwordEncoder.encode(newPassword));
			userRepository.save(user);
		}
	}
	
}
