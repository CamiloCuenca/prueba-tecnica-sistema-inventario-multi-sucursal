package com.camilocuenca.inventorysystem.config;

import com.camilocuenca.inventorysystem.Enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTUtils jwtUtils;

    public JwtAuthenticationFilter(JWTUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            Jws<Claims> jws = jwtUtils.parseJwt(token);
            Claims claims = jws.getBody();

            String subject = claims.getSubject();
            Object roleObj = claims.get("role");
            Object userIdObj = claims.get("userId");
            Object branchIdObj = claims.get("branchId");
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();

            if (roleObj != null) {
                String roleStr = String.valueOf(roleObj);
                // Map role string to Spring authority
                try {
                    Role role = Role.valueOf(roleStr);
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown role in token: {}", roleStr);
                }
            }

            // Use subject as principal (email). Put userId in credentials and branchId in details
            Object credentials = userIdObj != null ? String.valueOf(userIdObj) : null;
            Object details = branchIdObj != null ? String.valueOf(branchIdObj) : null;
            Authentication auth = new UsernamePasswordAuthenticationToken(subject, credentials, authorities);
            ((UsernamePasswordAuthenticationToken) auth).setDetails(details);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (JwtException e) {
            // Token inválido o expirado: limpiar contexto y continuar para que
            // Spring Security invoque AuthenticationEntryPoint/AccessDeniedHandler
            log.warn("JWT validation failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            // don't set response here; continue the filter chain
        }

        filterChain.doFilter(request, response);
    }
}
