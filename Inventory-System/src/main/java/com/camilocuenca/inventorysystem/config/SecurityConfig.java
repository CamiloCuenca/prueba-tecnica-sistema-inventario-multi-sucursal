package com.camilocuenca.inventorysystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JWTUtils jwtUtils;

    public SecurityConfig(JWTUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtils);

        RestAuthenticationEntryPoint authEntryPoint = new RestAuthenticationEntryPoint();
        RestAccessDeniedHandler accessDeniedHandler = new RestAccessDeniedHandler();

        // Habilitar CORS
        http.cors(Customizer.withDefaults());

        // Configuración con lambdas para compatibilidad clara
        http.csrf(csrf -> csrf.disable());

        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint).accessDeniedHandler(accessDeniedHandler));

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
