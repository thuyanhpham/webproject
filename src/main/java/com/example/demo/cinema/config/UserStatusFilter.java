package com.example.demo.cinema.config;

import com.example.demo.cinema.entity.Status;
import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.repository.UserRepository;
import com.example.demo.cinema.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
public class UserStatusFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails) {
                    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                    Long userId = userDetails.getUser().getId();
                    Status currentStatus = userRepository.findById(userId)
                        .map(User::getStatus)
                        .orElse(Status.DELETED);
                    if (currentStatus != Status.ACTIVE) {
                        new SecurityContextLogoutHandler().logout(request, response, authentication);
                        response.sendRedirect(request.getContextPath() + "/login?kicked=true");
                        return;
                    }
                }
                filterChain.doFilter(request, response);
            }
}
