package vn.manh.findJob.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import vn.manh.findJob.service.UserService;

import java.util.Collections;

@RequiredArgsConstructor
@Component("UserDetailsService")
public class UserDetailCustom implements UserDetailsService {
    private final UserService userService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        vn.manh.findJob.domain.User user=this.userService.handleGetUserByUserName(username);
        if (user == null) {
            // Ném ra exception chuẩn của Spring Security
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        return new User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
