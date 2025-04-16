package com.example.demo.cinema.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.example.demo.cinema.service.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Tắt CSRF trong môi trường dev
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                // ---- TÀI NGUYÊN TĨNH ----
                .requestMatchers(
                		 new AntPathRequestMatcher("/css/**"),
                         new AntPathRequestMatcher("/js/**"),
                         new AntPathRequestMatcher("/images/**"),
                         new AntPathRequestMatcher("/webfonts/**"),
                         new AntPathRequestMatcher("/fonts/**")
                ).permitAll()

                // ---- ENDPOINT CÔNG KHAI ----
                .requestMatchers(
                	    new AntPathRequestMatcher("/"),
                        new AntPathRequestMatcher("/movielist"),
                        // Sử dụng AntPathRequestMatcher cho các đường dẫn có biến
                        new AntPathRequestMatcher("/movie/{id}"), // Khớp /movie/bất_kỳ_id_nào
                        new AntPathRequestMatcher("/movies/{movieId}/showtimes"), // <<< SỬA Ở ĐÂY: Khớp chính xác cấu trúc controller
                        new AntPathRequestMatcher("/movie-details/**"),
                        new AntPathRequestMatcher("/login"),
                        new AntPathRequestMatcher("/auth/login"),
                        new AntPathRequestMatcher("/auth/register"),
                        new AntPathRequestMatcher("/auth/forgot-password"),
                        new AntPathRequestMatcher("/subscribe")
                ).permitAll()

                // ---- YÊU CẦU XÁC THỰC HOẶC ROLE ----
                .requestMatchers("/api/users/**").authenticated()
                .requestMatchers("/api/users").hasRole("ADMIN")

                // ---- TẤT CẢ ENDPOINT KHÁC ----
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                //.loginPage("/login") // Custom login page trả về view "login"
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/login?logout")
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

    // Cung cấp AuthenticationManager từ userService
//    @Bean
//    public AuthenticationManager authenticationManager() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(userService);
//        provider.setPasswordEncoder(passwordEncoder());
//        return new ProviderManager(provider);
//    }
}