package com.elams.elams_backend.service.auth;

import com.elams.elams_backend.dto.auth.AuthRequest;
import com.elams.elams_backend.dto.auth.AuthResponse;
import com.elams.elams_backend.dto.auth.RegisterRequest;
import com.elams.elams_backend.entity.Role;
import com.elams.elams_backend.entity.User;
import com.elams.elams_backend.repository.RoleRepository;
import com.elams.elams_backend.repository.UserRepository;
import com.elams.elams_backend.security.jwt.JwtService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    // =========================
    // REGISTER USER
    // =========================
    public String register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Role defaultRole = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .roles(Set.of(defaultRole))
                .build();

        userRepository.save(user);

        return "User registered successfully";
    }

    // =========================
    // LOGIN USER
    // =========================
    public AuthResponse login(AuthRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(
                userDetails.getUsername(),
                new HashMap<>()
        );

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration)
                .build();
    }
}
