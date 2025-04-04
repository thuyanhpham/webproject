package com.example.demo.cinema.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
					.logoutUrl("/auth/login")
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
	

}
