package com.example.demo.cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.cinema.dto.CommonResult;
import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.repository.RoleRepository;
import com.example.demo.cinema.service.UserService;

@Controller
@RequestMapping("")
public class AuthController {
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private UserService userService;
	@Autowired
	private RoleRepository roleRepository;

	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("pageTitle", "Boleto - Online Movie Ticket Booking");
		return "index";
	}

	// API Đăng ký
	@GetMapping("/register")
	public String showRegisterForm(Model model) {
		model.addAttribute("user", new User());
		return "register";
	}

	public String register(@RequestParam String fullname,
							@RequestParam String username,
							@RequestParam String password,
							@RequestParam String confirmPassword,
							@RequestParam String email,
							RedirectAttributes redirectAttributes) {

		CommonResult result = userService.registerUser(email, username, password, confirmPassword, fullname);

		if (!result.isSuccess()) {
			redirectAttributes.addFlashAttribute("error", result.getMessage());
			return "redirect:/register";
		}

		redirectAttributes.addFlashAttribute("success", result.getMessage());
		return "redirect:/login";
	}

	// Đăng nhập
	@GetMapping("/login")
	public String LoginPage() {
		return "login";
	}

	// API Quên mật khẩu
	@GetMapping("/forgot-password")
	public String showForgotPasswordForm() {
		return "user/forgot-password";
	}

	@PostMapping("/forgot-password")
	public String processForgotPassword(
			@RequestParam("email") String email,
			@RequestParam("password") String password,
			@RequestParam("confirmPassword") String confirmPassword,
			RedirectAttributes redirectAttributes) {

		if (!password.equals(confirmPassword)) {
			redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp.");
			return "redirect:/user/forgot-password";
		}

		String result = userService.updatePasswordByEmail(email, password);
		if (result.contains("thành công")) {
			redirectAttributes.addFlashAttribute("success", result + " Vui lòng đăng nhập.");
			return "redirect:/login";
		} else {
			redirectAttributes.addFlashAttribute("error", result);
			return "redirect:/user/forgot-password";
		}
	}

}
