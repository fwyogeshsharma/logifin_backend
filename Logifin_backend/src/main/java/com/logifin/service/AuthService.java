package com.logifin.service;

import com.logifin.dto.AuthResponse;
import com.logifin.dto.LoginRequest;
import com.logifin.dto.RegisterRequest;

public interface AuthService {

    AuthResponse login(LoginRequest loginRequest);

    AuthResponse register(RegisterRequest registerRequest);
}
