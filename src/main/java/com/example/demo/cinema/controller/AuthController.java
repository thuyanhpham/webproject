package com.example.demo.cinema.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.cinema.dto.AuthResponse;
import com.example.demo.cinema.dto.LoginRequest;
import com.example.demo.cinema.dto.RegisterRequest;
import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.security.JwtUtil;
import com.example.demo.cinema.service.PasswordResetService;
import com.example.demo.cinema.service.TokenBlacklistService;
import com.example.demo.cinema.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
//test
	private UserService userService;
	private PasswordEncoder passwordEncoder;
	
	
	
	
	//Xử lý đăng ký
	@PostMapping("/register")
	public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
		if (userService.existByUsername(request.getUsername())) {
			return ResponseEntity.badRequest().body("Tên đăng nhập đã tồn tại!");
		}
		
		if (userService.existByEmail(request.getEmail())) {
			return ResponseEntity.badRequest().body("Email đã được sử dụng!");
		}
		
		
		return ResponseEntity.ok("Đăng ký thành công!");
	}
	
	//Xử lý đăng nhập
	@PostMapping("/login")
	public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
		Optional<User> user = userService.authenticateUser(request.getUsernameOrEmail(), request.getPassword());
		
		if (user.isPresent()) {
			String token = jwtUtil.generateToken(user.get().getUsername());
			
			AuthResponse authResponse = new AuthResponse();
			return ResponseEntity.ok(authResponse);
		} else {
			return ResponseEntity.badRequest().body("Tên đăng nhập hoặc mật khẩu không đúng!");
		}
		
	}
	
	//API quên mật khẩu
	@PostMapping("/forgot-password")
	public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
		String email = request.get("email");
		
		if (!userService.existByEmail(email)) {
			return ResponseEntity.badRequest().body("Email không tồn tại trong hệ thống!");
			
		}
		
		String token = passwordResetService.createdResetToken(email);
		
		return ResponseEntity.ok("Mã đặt lại mật khẩu đã được gửi qua email!");
	}
	
	//API đặt lại mật khẩu
	@PostMapping("/reset-password")
	public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
		String token = request.get("token");
		String newPassword = request.get("newPassword");
		
		//Kiểm tra token hợp lệ
		Optional<PasswordResetToken> resetTokenOpt = passwordResetService.validateResetToken(token);
		if (resetTokenOpt.isEmpty()) {
			return ResponseEntity.badRequest().body("Token không hợp lệ hoặc đã hết hạn!");
			
		}
		
		PasswordResetToken resetToken = resetTokenOpt.get();
		String email = resetToken.getEmail();
		
		//Đặt lại mật khẩu mới
		userService.updatePasswordByEmail(email, newPassword);
		
		//Xóa token sau khi sử dụng
		passwordResetTokenRepository.delete(resetToken);
		
		return ResponseEntity.ok("Mật khẩu đã được đặt lại thành công!");
	}
	
	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpServletRequest request) {
	String authHeader = request.getHeader("Authorization");
	if (authHeader != null && authHeader.startsWith("Bearer ")) {
		String token = authHeader.substring(7);
		tokenBlacklistService.blacklistToken(token);
	}
	SecurityContextHolder.clearContext();
	return ResponseEntity.ok("Đăng xuất thành công!");
  }
}
