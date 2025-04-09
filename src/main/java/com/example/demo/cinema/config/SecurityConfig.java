package com.example.demo.cinema.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
<<<<<<< HEAD
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
=======
// Bỏ import không dùng đến nếu chỉ cấu hình SecurityFilterChain cơ bản
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
>>>>>>> a438f6641e1fbea9a4705a862a9de4f583c54063
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

<<<<<<< HEAD
import com.example.demo.cinema.service.UserService;
=======
// UserService chỉ cần nếu bạn tạo UserDetailsService bean ở đây
// import com.example.demo.cinema.service.UserService;
>>>>>>> a438f6641e1fbea9a4705a862a9de4f583c54063

@Configuration
@EnableWebSecurity
public class SecurityConfig {

<<<<<<< HEAD
	private UserService userService;
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
			.authorizeHttpRequests(auth -> auth
					.requestMatchers("/auth/login", "/auth/register", "/auth/forgot-passord").permitAll()
					.requestMatchers("/api/users/**").authenticated()
					.requestMatchers("/api/users").hasRole("ADMIN")
					.anyRequest().authenticated()
			)
			.formLogin(form -> form
					.loginPage("/auth/login")
					.defaultSuccessUrl("/home", true)
					.permitAll()
			)
			.logout(logout -> logout
					.logoutUrl("/auth/logout")
					.logoutSuccessUrl("/auth/login")
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
	
	@Bean 
	public AuthenticationManager authenticationManeger() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userService);
		provider.setPasswordEncoder(passwordEncoder());
		return new ProviderManager(provider);
	}
}
=======
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
>>>>>>> a438f6641e1fbea9a4705a862a9de4f583c54063
