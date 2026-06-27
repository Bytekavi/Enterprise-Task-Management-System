package com.enterprise.tasks.service;

import com.enterprise.tasks.api.dto.AuthDtos.LoginRequest;
import com.enterprise.tasks.api.dto.AuthDtos.RegisterRequest;
import com.enterprise.tasks.api.dto.AuthDtos.TokenResponse;
import com.enterprise.tasks.domain.Role;
import com.enterprise.tasks.domain.User;
import com.enterprise.tasks.repository.UserRepository;
import com.enterprise.tasks.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Email is already registered");
        }
        User user = users.save(new User(
            request.email(), passwordEncoder.encode(request.password()), Role.USER));
        return new TokenResponse(jwtService.createToken(user.getEmail()));
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        return new TokenResponse(jwtService.createToken(request.email().toLowerCase()));
    }
}

