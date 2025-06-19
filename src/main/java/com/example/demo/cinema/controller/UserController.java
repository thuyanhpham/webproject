package com.example.demo.cinema.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.demo.cinema.entity.Status;
import com.example.demo.cinema.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

	private final UserService userService;
	public UserController(UserService userService) {
		this.userService = userService;
	}
	
	@GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users/user-list";
    }

	@PostMapping("/{id}/status")
	public String updateUserStatus(@PathVariable("id") Long id, @RequestParam("status") Status status) {
		userService.updateUserStatus(id, status);
		String action = (status == Status.ACTIVE) ? "mở" : "đóng";
		return "redirect:/admin/users";
	}
	
	@PostMapping("/{id}/delete")
	public String deleteUser(@PathVariable("id") Long id) {
		userService.deleteUser(id);
		return "redirect:/admin/users";
	}
	
}
