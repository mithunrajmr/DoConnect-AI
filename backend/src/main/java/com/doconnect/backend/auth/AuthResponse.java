package com.doconnect.backend.auth;

public record AuthResponse(String token, UserResponse user) {
}
