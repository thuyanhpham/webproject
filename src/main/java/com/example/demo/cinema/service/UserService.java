package com.example.demo.cinema.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.cinema.entity.Role;
import com.example.demo.cinema.entity.Status;
import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.repository.RoleRepository;
import com.example.demo.cinema.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private RoleRepository roleRepository;
	
	public UserService(PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
		this.passwordEncoder = passwordEncoder;
		this.roleRepository = roleRepository;
	}
	
	// Đăng ký user
	public String registerUser(String email, String username, String password, String fullname) {
		if (userRepository.findByEmail(email).isPresent()) {
			return "Email đã tồn tại!";
		}
		
		if (userRepository.findByUsername(username).isPresent()) {
			return "Username đã tồn tại!";
		}
		
		
		Role userRole = roleRepository.findByName("USER")
				.orElseThrow(() -> new RuntimeException("Không tìm thấy role USER"));
		
		User user = new User();
		user.setEmail(email);
		user.setUsername(username);
		user.setPassword(passwordEncoder.encode(password));
		user.setFullname(fullname);
		user.setRole(userRole);
		userRepository.save(user);
		return "Đăng ký thành công!";
	}
	
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy User"));
		
		return new org.springframework.security.core.userdetails.User(user.getUsername(),
						user.getPassword(),
						List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())));
	}
	
	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}
	
	// Cập nhật mật khẩu
	public String updatePasswordByEmail(String email, String newPassword) {
		email = email.trim().toLowerCase();
		
	    Optional<User> optionalUser = userRepository.findByEmailIgnoreCase(email);
	    if (optionalUser.isPresent()) {
	        User user = optionalUser.get();
	        user.setPassword(passwordEncoder.encode(newPassword));
	        userRepository.save(user);
	        return "Cập nhật mật khẩu thành công.";
	    }
	    return "Không tìm thấy người dùng với email này.";
	}

	
	public void save(User user) {
		userRepository.save(user);
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
