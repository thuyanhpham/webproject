package com.example.demo.cinema.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.cinema.service.UserService;

@Controller
public class ViewController {

	private UserService userService;
	
	@GetMapping("/register")
	public String showRegisterForm() {
		return "signup";
	}
}
