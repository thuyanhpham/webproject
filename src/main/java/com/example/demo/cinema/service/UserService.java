package com.example.demo.cinema.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.example.demo.cinema.security.CustomUserDetails;

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
	
	public String registerUser(String email, String username, String password, String fullname) {
		if (userRepository.findByEmail(email).isPresent()) {
			return "Email đã tồn tại!";
		}
		
		if (userRepository.findByUsername(username).isPresent()) {
			return "Username đã tồn tại!";
		}
		
		Role userRole = roleRepository.findByName("ROLE_USER")
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
		return new CustomUserDetails(user);
	}
	
	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}
	
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
	
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}
	
	public User getUserById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found"));
	}
	
	public User updateUser(Long id, User updatedUser) {
		User user = getUserById(id);
		user.setFullname(updatedUser.getFullname());
		user.setPhone(updatedUser.getPhone());
		user.setGender(updatedUser.getGender());
		user.setBirthday(updatedUser.getBirthday());
		return userRepository.save(user);
	}
	
	public void changePassword(Long id, String oldPassword, String newPassword) {
		User user = getUserById(id);
		if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
			throw new RuntimeException("Mật khẩu cũ không đúng");
		}
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
		
	}
	
	public User updateUserStatus(Long id, Status status) {
		User user = getUserById(id);
		user.setStatus(status);
		return userRepository.save(user);
	}
	
	public void deleteUser(Long id) {
		userRepository.deleteById(id);
	}
}
