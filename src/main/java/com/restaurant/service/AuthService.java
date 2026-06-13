package com.restaurant.service;

import com.restaurant.config.JwtUtil;
import com.restaurant.dto.JwtResponseDTO;
import com.restaurant.dto.LoginRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    public JwtResponseDTO login(LoginRequestDTO request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Extract role — Spring Security prefixes roles with "ROLE_"
        String role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_UNKNOWN")
                .replace("ROLE_", "");

        String token = jwtUtil.generateToken(request.getUsername(), role);

        return JwtResponseDTO.builder()
                .token(token)
                .username(request.getUsername())
                .role(role)
                .expiresIn(expirationMs)
                .build();
    }
}
