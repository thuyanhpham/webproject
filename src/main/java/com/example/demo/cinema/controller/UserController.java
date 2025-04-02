package com.example.demo.cinema.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private UserService userService;
	
	// Lấy danh sách tất cả user (Chỉ Admin mới được truy cập)
	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<User>> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
		
	}
	
	// Lấy thông tin cá nhân
	@GetMapping("/{id}")
	public ResponseEntity<User> getUserById(@PathVariable Long id) {
		return ResponseEntity.ok(userService.getUserById(id));
	}
	
	// Đổi mật khẩu
	@PostMapping("/{id}/change-password")
	public ResponseEntity<String> changePassword(@PathVariable Long id, @RequestParam String oldPassword, @RequestParam String newPassword) {
		userService.changePassword(id, oldPassword, newPassword);
		return ResponseEntity.ok("Đổi mật khẩu thành công");
	}
	
	// Cập nhật trạng thái tài khoản (Admin quyền)
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return ResponseEntity.ok("Xóa tài khoản thành công");
	}
	
	// Cập nhật thông tin người dùng
	@PutMapping("/{id}")
	public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
		try {
			User updated = userService.updateUser(id, updatedUser);
			return ResponseEntity.ok(updated);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}
}
