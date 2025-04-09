package com.example.demo.cinema.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// Bỏ import không dùng đến nếu chỉ cấu hình SecurityFilterChain cơ bản
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// UserService chỉ cần nếu bạn tạo UserDetailsService bean ở đây
// import com.example.demo.cinema.service.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

		@Bean
		public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
			http
				.csrf(csrf -> csrf.disable()) // Xem xét bật lại trong môi trường production
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
				.authorizeHttpRequests(auth -> auth
						// Cho phép truy cập tài nguyên tĩnh
						.requestMatchers(
							"/css/**",
							"/js/**",
							"/images/**",
							"/webfonts/**", // Đã thêm
							"/fonts/**"     // Đã thêm
						).permitAll()
						// Cho phép truy cập các trang công khai và trang login
						.requestMatchers(
							"/auth/login",          // URL xử lý login (POST) nếu có
							"/auth/register",
							"/auth/forgot-password",
							"/movielist",           // Trang danh sách phim
							"/",                    // Trang gốc (nếu có)
							"/login"                // <<<--- THÊM DÒNG NÀY: Cho phép truy cập trang login
						).permitAll()
						// Các quy tắc bảo mật khác
						.requestMatchers("/api/users/**").authenticated()
						.requestMatchers("/api/users").hasRole("ADMIN")
						.anyRequest().authenticated() // Mọi request khác phải xác thực
				)
				.formLogin(form -> form
						//.loginPage("/login") // Chỉ định trang login tùy chỉnh
						// ====> SỬA URL THÀNH CÔNG SAU LOGIN <====
						.defaultSuccessUrl("/movielist", true) // Chuyển đến movielist sau khi login
						// =======================================
						.permitAll() // Cho phép request POST đến /login để xử lý
				)
				.logout(logout -> logout
						.logoutUrl("/auth/logout") // URL để logout
						// ====> SỬA URL THÀNH CÔNG SAU LOGOUT <====
						.logoutSuccessUrl("/login?logout") // Chuyển về trang login với tham số
						// =========================================
						.invalidateHttpSession(true)
						.deleteCookies("JSESSIONID")
						.permitAll()
				);

			return http.build();
		}

		@Bean
		public PasswordEncoder passwordEncoder() {
			return new BCryptPasswordEncoder();
		}

		// ---- QUAN TRỌNG: Nhắc nhở về UserDetailsService ----
		// Bạn CẦN phải có một Bean UserDetailsService để Spring Security
		// biết cách tải thông tin người dùng (username, password, roles/authorities).
		// Nếu UserService của bạn implement UserDetailsService và được đánh dấu @Service,
		// Spring có thể tự động tìm thấy nó.
		// Nếu không, bạn cần tạo một Bean như sau:
		/*
		@Autowired
		private UserService userService; // Giả sử UserService implement UserDetailsService

		@Bean
		public UserDetailsService userDetailsService() {
		    return userService;
		}
		*/
		// -----------------------------------------------------
}