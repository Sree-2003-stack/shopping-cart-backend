package com.securecart.backend.service;

import com.securecart.backend.dto.AuthResponse;
import com.securecart.backend.dto.LoginRequest;
import com.securecart.backend.dto.MessageResponse;
import com.securecart.backend.dto.RegisterRequest;

public interface AuthService {
    MessageResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
