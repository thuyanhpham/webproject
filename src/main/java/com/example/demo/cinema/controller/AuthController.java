package com.example.demo.cinema.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.repository.UserRepository;
import com.example.demo.cinema.service.UserService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private UserService userService;
	
	//API Đăng ký
	@PostMapping("/register")
	public Map<String, String> register(@RequestParam String username, @RequestParam String password, @RequestParam String email) {
		Map<String, String> response = new HashMap<>();
		if (userRepository.findByUsername(username).isPresent()) {
			response.put("message", "Username đã tồn tại");
			return response;
		}
		
		User user =new User();
		user.setUsername(username);
		user.setPassword(passwordEncoder.encode(password));
		user.setEmail(email);
		userRepository.save(user);
		
		response.put("message", "Đăng ký thành công");
		return response;
		
	}
	
	// API Đăng nhập
	@PostMapping("/login")
	public Map<String, String> login(@RequestParam String username, @RequestParam String password, HttpSession session) {
		Map<String, String> response = new HashMap<>();
		Optional<User> optionalUser = userService.findByUsername(username);
		
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (new BCryptPasswordEncoder().matches(password, user.getPassword())) {
				session.setAttribute("user", user);
				response.put("message", "Đăng nhập thành công!");
				
				// Kiểm tra nếu role không null và gọi toString() trên đối tượng role
				if (user.getRole() != null) {
					response.put("role", user.getRole().toString());
				} else {
					response.put("role", "UNKNOWN");
				}
		} else {
			response.put("message",  "Tên đăng nhập hoặc mật khẩu không đúng!");
		}
	} 
		return response; 
	}
	
	// API Kiểm tra trạng thái đăng nhập
	@GetMapping("/check-login")
	public Map<String, String> checkLogin(HttpSession session) {
		Map<String, String> response = new HashMap<>();
		if (session.getAttribute("username") != null) {
			response.put("message", "Đã đăng nhập");
			response.put("username", session.getAttribute("username").toString());
			
		} else {
			response.put("message", "Chưa đăng nhập");
		}
		return response;
	}
	
	// API Đăng xuất
	@PostMapping("/logout")
	public Map<String, String> logout(HttpSession session) {
		session.invalidate();
		Map<String, String> response = new HashMap<>();
		response.put("message", "Đăng xuất thành công");
		return response;
	}
	
	// API Quên mật khẩu
	@PostMapping("/forgot-password")
	public Map<String, String> forgotPassword(@RequestParam String email) {
		Optional<User> userOpt = userRepository.findByEmail(email);
		Map<String, String> response = new HashMap<>();
		
		if (userOpt.isEmpty()) {
			response.put("message", "Email không tồn tại");
			return response;
		}
		
		User user = userOpt.get();
		String newPassword = "123456";
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
		
		response.put("message", "Mật khẩu mới đã được đặt: " + newPassword);
		return response;
	}
	
	// cimema list
	
	
}
