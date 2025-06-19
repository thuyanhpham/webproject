package com.example.demo.cinema.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.demo.cinema.entity.Status;
import com.example.demo.cinema.entity.User;
import com.example.demo.cinema.service.UserService;
import java.io.IOException;
import java.util.Optional;

@Component
public class BlockedUserFilter extends OncePerRequestFilter {

    private final UserService userService;

    public BlockedUserFilter(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
        throws ServletException, IOException {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof UserDetails) {
                        String username = authentication.getName();
                        Optional<User> user = userService.findByUsername(username);

                        if (!user.isPresent() || user.get().getStatus() != Status.ACTIVE) {
                            SecurityContextHolder.clearContext();
                            request.getSession().invalidate();
                            response.sendRedirect("/login?expired");
                            return;
                        }
                    }
                    filterChain.doFilter(request, response);
        }
}
