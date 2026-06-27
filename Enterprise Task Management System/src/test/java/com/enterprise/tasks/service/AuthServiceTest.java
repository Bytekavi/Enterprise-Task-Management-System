package com.enterprise.tasks.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.enterprise.tasks.api.dto.AuthDtos.*;
import com.enterprise.tasks.domain.User;
import com.enterprise.tasks.repository.UserRepository;
import com.enterprise.tasks.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {
    private UserRepository users;
    private PasswordEncoder encoder;
    private AuthenticationManager authenticationManager;
    private JwtService jwt;
    private AuthService service;

    @BeforeEach void setUp() {
        users = mock(UserRepository.class);
        encoder = mock(PasswordEncoder.class);
        authenticationManager = mock(AuthenticationManager.class);
        jwt = mock(JwtService.class);
        service = new AuthService(users, encoder, authenticationManager, jwt);
        when(encoder.encode(anyString())).thenReturn("encoded");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwt.createToken(anyString())).thenReturn("token");
    }

    @Test void registrationReturnsToken() {
        assertEquals("token",
            service.register(new RegisterRequest("new@example.com", "strong-password")).accessToken());
    }

    @Test void duplicateRegistrationIsRejected() {
        when(users.existsByEmailIgnoreCase("taken@example.com")).thenReturn(true);
        assertThrows(ConflictException.class,
            () -> service.register(new RegisterRequest("taken@example.com", "strong-password")));
    }

    @Test void registrationHashesPassword() {
        service.register(new RegisterRequest("new@example.com", "strong-password"));
        verify(encoder).encode("strong-password");
    }

    @Test void registrationNeverStoresPlainPassword() {
        service.register(new RegisterRequest("new@example.com", "strong-password"));
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(users).save(captor.capture());
        assertEquals("encoded", captor.getValue().getPasswordHash());
    }

    @Test void failedAuthenticationDoesNotIssueToken() {
        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("bad credentials"));
        assertThrows(BadCredentialsException.class,
            () -> service.login(new LoginRequest("user@example.com", "wrong-password")));
        verify(jwt, never()).createToken(anyString());
    }
}

