package com.example.demo.cinema.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String targetUrl = determineTargetUrl(authentication);

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to {}", targetUrl);
            return;
        }

        log.info("Redirecting user {} to {}", authentication.getName(), targetUrl);
        redirectStrategy.sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        for (GrantedAuthority grantedAuthority : authorities) {
            String authorityName = grantedAuthority.getAuthority();
            log.debug("User has authority: {}", authorityName);

            if (authorityName.equals("ROLE_ADMIN")) {
                log.debug("User is ADMIN. Redirecting to /admin");
                return "/admin"; // URL cho Admin Dashboard
            }
            // Bạn có thể thêm kiểm tra ROLE_USER nếu muốn chắc chắn hơn
            // else if (authorityName.equals("ROLE_USER")) {
            //     log.debug("User is USER. Redirecting to /home");
            //     return "/home"; // URL cho User Home
            // }
        }

        // Mặc định cho các vai trò khác (bao gồm cả ROLE_USER nếu không kiểm tra riêng)
        log.debug("User is not ADMIN or has other roles. Redirecting to /home");
        return "/home"; // <<< THAY ĐỔI Ở ĐÂY: Chuyển hướng user thường về /home
    }
}
