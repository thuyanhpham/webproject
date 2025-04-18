package com.example.demo.cinema.config; // Đảm bảo package này đúng với cấu trúc dự án của bạn

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Bật nếu dùng @PreAuthorize
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService; // Import interface chuẩn
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

// import com.example.demo.cinema.service.UserService; // Không cần import trực tiếp nếu UserService implements UserDetailsService

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // <-- Bật để @PreAuthorize trong UserController hoạt động
public class SecurityConfig {

    // Inject UserDetailsService (Lớp UserService của bạn phải implement interface này)
    @Autowired
    @Lazy // Giữ lại @Lazy để phòng tránh circular dependency
    private UserDetailsService userDetailsService;

    /**
     * Bean cung cấp cơ chế mã hóa mật khẩu (BCrypt).
     * Bắt buộc phải có để Spring Security hoạt động.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean cấu hình Authentication Provider sử dụng UserDetailsService và PasswordEncoder.
     * Nó chịu trách nhiệm xác thực thông tin đăng nhập của người dùng.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Cung cấp cách lấy thông tin user
        authProvider.setPasswordEncoder(passwordEncoder());    // Cung cấp cách mã hóa/so sánh mật khẩu
        return authProvider;
    }

    /**
     * Bean cung cấp AuthenticationManager chuẩn từ Spring Boot.
     * Được sử dụng nội bộ bởi Spring Security.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Bean chính cấu hình chuỗi bộ lọc bảo mật (Security Filter Chain).
     * Định nghĩa các quy tắc bảo mật cho ứng dụng web.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Đăng ký AuthenticationProvider đã tạo
            .authenticationProvider(authenticationProvider())

            // Tắt CSRF protection (Phổ biến cho API, cân nhắc bật lại cho ứng dụng web chỉ dùng form)
            .csrf(csrf -> csrf.disable())

            // Quản lý Session: Tạo session khi cần thiết (phù hợp với form login)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

            // Định nghĩa các quy tắc ủy quyền (authorization) cho các request HTTP
            .authorizeHttpRequests(auth -> auth
                    // ---- CHO PHÉP TRUY CẬP CÔNG KHAI (permitAll) ----
                    .requestMatchers(
                        // Tài nguyên tĩnh (CSS, JS, Hình ảnh, Fonts)
                        new AntPathRequestMatcher("/css/**"),
                        new AntPathRequestMatcher("/js/**"),
                        new AntPathRequestMatcher("/images/**"),
                        new AntPathRequestMatcher("/webfonts/**"),
                        new AntPathRequestMatcher("/fonts/**"),
                        new AntPathRequestMatcher("/favicon.ico"),

                        // Trang chủ
                        new AntPathRequestMatcher("/"),

                        // Các endpoint liên quan đến Xác thực / Đăng ký / Quên mật khẩu
                        new AntPathRequestMatcher("/login"),                 // Trang hiển thị form login
                        new AntPathRequestMatcher("/login"),            // API xử lý login (nếu còn dùng)
                        new AntPathRequestMatcher("/register"),         // API đăng ký
                        new AntPathRequestMatcher("/forgot-password") // API quên mật khẩu
                                   

                    ).permitAll() // Cho phép tất cả các request khớp với các mẫu trên

                    // ---- TẤT CẢ CÁC YÊU CẦU KHÁC ----
                    // Bất kỳ request nào không khớp với các quy tắc permitAll ở trên
                    // đều yêu cầu người dùng phải được xác thực (đăng nhập).
                    .anyRequest().authenticated()
            )

            // ---- Cấu hình Form Login ----
            .formLogin(form -> form
                    .loginPage("/login")              // Đường dẫn đến trang login tùy chỉnh của bạn
                    // .loginProcessingUrl("/auth/login") // Bỏ comment nếu muốn API POST /auth/login xử lý thay vì Spring Security
                    .defaultSuccessUrl("/home", true)   // **Chuyển đến /home sau khi đăng nhập thành công**
                    .failureUrl("/login?error")         // Chuyển về trang login với tham số lỗi nếu thất bại
                    .permitAll()                      // Cho phép mọi người truy cập cơ chế login
            )

            // ---- Cấu hình Logout ----
            .logout(logout -> logout
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                    .logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll()
            );

        // Xây dựng và trả về đối tượng cấu hình HttpSecurity
        return http.build();
    }
}