package com.example.hrms.security;

import com.example.hrms.biz.user.model.User;
import com.example.hrms.enumation.RoleEnum;
import com.example.hrms.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private static List<GrantedAuthority> getGrantedAuthorities(String role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));
        return authorities;
    }

    public static Collection<? extends GrantedAuthority> getAuthorities(
        RoleEnum role) {

        return getGrantedAuthorities(role.name());
    }

    public static Stream<String> getListAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority);
    }

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Unauthorized");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return ((User) authentication.getPrincipal()).getUsername();
        } else {
            return principal.toString();
        }
    }

    public static String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Unauthorized");
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("No role found"));
    }
}
