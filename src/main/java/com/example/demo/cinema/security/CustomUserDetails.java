package com.example.demo.cinema.security;
 
import java.util.Collection;
import java.util.Collections;
 
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.demo.cinema.entity.Status;
import com.example.demo.cinema.entity.User;
 
public class CustomUserDetails implements UserDetails {
 
	private static final long serialVersionUID = 1L;
	private User user;
	
	public CustomUserDetails(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}
	
	public String getFullname() {
		return user.getFullname();
	}
	
	public String getEmail() {
		return user.getEmail();
	}
 
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		 if (user.getRole() == null || user.getRole().getName() == null) {
            
             return Collections.emptySet();
        }
		return Collections.singleton(new SimpleGrantedAuthority(user.getRole().getName()));
	}
 
	@Override
	public String getPassword() {
		return user.getPassword();
	}
 
	@Override
	public String getUsername() {
		return user.getUsername();
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}
	
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}
 
	@Override
	public boolean isEnabled() {
	    return user.getStatus() == Status.ACTIVE;
	}
	
}