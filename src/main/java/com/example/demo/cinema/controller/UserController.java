package com.example.demo.cinema.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.cinema.entity.Status;
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
	
	//Cập nhật thông tin cá nhân
	@PutMapping("/{id}")
	public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updateUser) {
		return ResponseEntity.ok(userService.updateUser(id, updateUser));
	}
	
	// Đổi mật khẩu
	@PostMapping("/{id}/change-password")
	public ResponseEntity<String> changePassword(@PathVariable Long id, @RequestParam String oldPassword, @RequestParam String newPassword) {
		userService.changePassword(id, oldPassword, newPassword);
		return ResponseEntity.ok("Đổi mật khẩu thành công");
	}
	
	// Cập nhật trạng thái tài khoản (Admin)
	@PatchMapping("/{id}/status")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<User> updateUserStatus(@PathVariable Long id, @RequestParam Status status) {
		return ResponseEntity.ok(userService.updateUserStatus(id, status));
	}
	
	// Xóa tài khoản (Admin)
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return ResponseEntity.ok("Xóa tài khoản thành công!");
	}
}
