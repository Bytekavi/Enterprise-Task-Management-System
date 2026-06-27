package com.enterprise.tasks.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private static final String SECRET = "a-secure-test-secret-with-more-than-32-characters";

    @Test void tokenRoundTripPreservesSubject() {
        JwtService service = new JwtService(SECRET, 30);
        assertEquals("user@example.com", service.extractSubject(
            service.createToken("user@example.com")));
    }

    @Test void tokenIsNotPlainTextCredential() {
        JwtService service = new JwtService(SECRET, 30);
        assertFalse(service.createToken("user@example.com").contains(SECRET));
    }

    @Test void wrongSigningKeyIsRejected() {
        JwtService issuer = new JwtService(SECRET, 30);
        JwtService verifier = new JwtService("another-secure-secret-with-more-than-32-characters", 30);
        assertThrows(RuntimeException.class,
            () -> verifier.extractSubject(issuer.createToken("user@example.com")));
    }

    @Test void malformedTokenIsRejected() {
        JwtService service = new JwtService(SECRET, 30);
        assertThrows(RuntimeException.class, () -> service.extractSubject("not-a-jwt"));
    }

    @Test void shortSecretIsRejectedAtStartup() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("short", 30));
    }
}

