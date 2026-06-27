package com.enterprise.tasks.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 12, max = 128) String password
    ) {}

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

    public record TokenResponse(String accessToken, String tokenType) {
        public TokenResponse(String accessToken) {
            this(accessToken, "Bearer");
        }
    }
}

