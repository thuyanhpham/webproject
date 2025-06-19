package com.example.demo.cinema.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.cinema.entity.Role;
import com.example.demo.cinema.entity.Status;
import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.exception.ResourceNotFoundException;
import com.example.demo.cinema.repository.RoleRepository;
import com.example.demo.cinema.repository.UserRepository;
import com.example.demo.cinema.security.CustomUserDetails;

@Service
public class UserService implements UserDetailsService {

	@Autowired
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RoleRepository roleRepository;
	private final SessionRegistry sessionRegistry;

	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, SessionRegistry sessionRegistry) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.roleRepository = roleRepository;
		this.sessionRegistry = sessionRegistry;
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
		// Sử dụng method mới đã có JOIN FETCH
		User user = userRepository.findByUsernameWithRole(username) 
				.orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy User với username: " + username));

		if (user.getStatus() != Status.ACTIVE) {
			throw new DisabledException("User account is " + user.getStatus().name());
		}
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
		return userRepository.findByStatusNot(Status.DELETED);
	}
	
	public User getUserById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("User not found"));
	}
	
	public void updateUserStatus(Long userId, Status newStatus) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
		user.setStatus(newStatus);
		userRepository.save(user);
		if (newStatus == Status.BANNED || newStatus == Status.DELETED) {
			invalidateUserSessions(user.getUsername());
		}
	}
	
	public void deleteUser(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
		user.setStatus(Status.DELETED);
		userRepository.save(user);
		invalidateUserSessions(user.getUsername());
	}

	private void invalidateUserSessions(String username) {
		List<Object> principals = sessionRegistry.getAllPrincipals();
		for (Object principal : principals) {
			if (principal instanceof UserDetails userDetails && userDetails.getUsername().equals(username)) {
				List<SessionInformation> sessions = sessionRegistry.getAllSessions(userDetails, false);
				for (SessionInformation session : sessions) {
					session.expireNow();
				}
			}
		}
	}
}
