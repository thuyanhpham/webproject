package com.example.demo.cinema.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.cinema.entity.Status;
import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class UserService implements UserDetailsService {
	
	private UserRepository userRepository;
	private BCryptPasswordEncoder passwordEncoder;
	private HttpSession session;
	
	// Đăng ký user
	public String registerUser(String email, String password, String fullname) {
		if (userRepository.findByEmail(email).isPresent()) {
			return "Email đã tồn tại!";
		}
		User user = new User();
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(password));
		user.setFullname(fullname);
		userRepository.save(user);
		return "Đăng ký thành công!";
	}
	
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy User"));
		
		return new org.springframework.security.core.userdetails.User(user.getUsername(),
						user.getPassword(),
						List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
	}
	
	
//	// Đăng nhập sử dụng sesion
//	public String loginUser(String email, String password) {
//		Optional<User> optionalUser = userRepository.findByEmail(email);
//		if (optionalUser.isPresent()) {
//			User user = optionalUser.get();
//			if (passwordEncoder.matches(password, user.getPassword())) {
//				session.setAttribute("user", user);
//				return "Đăng nhập thành công!";
//			}
//		}
//		return "Email hoặc mật khẩu không chính xác!";
//	}
	
	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}
	
	
//	// Đăng xuất
//	public String logoutUser() {
//		session.invalidate();
//		return "Đăng xuất thành công!";
//	}
	
	// Quên mật khẩu
	public String forgotPassword(String email) {
		Optional<User> optionalUser = userRepository.findByEmail(email);
		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			String newPassword = "12345678";
			user.setPassword(passwordEncoder.encode(newPassword));
			userRepository.save(user);
			return "Mật khẩu mới đã được đặt thành công!";
		}
		return "Email không tồn tại!";
	}
	
	// Lấy danh sách tất cả user (Admin)
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}
	
	// Lấy thông tin user theo ID
	public User getUserById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found"));
	}
	
	// Cập nhật thông tin cá nhân
	public User updateUser(Long id, User updatedUser) {
		User user = getUserById(id);
		user.setFullname(updatedUser.getFullname());
		user.setPhone(updatedUser.getPhone());
		user.setGender(updatedUser.getGender());
		user.setBirthday(updatedUser.getBirthday());
		return userRepository.save(user);
	}
	
	// Đổi mật khẩu
	public void changePassword(Long id, String oldPassword, String newPassword) {
		User user = getUserById(id);
		if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
			throw new RuntimeException("Mật khẩu cũ không đúng");
		}
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
		
	}
	
	// Cập nhật trạng thái tài khoản (Admin)
	public User updateUserStatus(Long id, Status status) {
		User user = getUserById(id);
		user.setStatus(status);
		return userRepository.save(user);
	}
	
	// Xóa tài khoản (Admin)
	public void deleteUser(Long id) {
		userRepository.deleteById(id);
	}


}
