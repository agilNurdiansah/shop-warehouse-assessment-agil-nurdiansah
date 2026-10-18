package com.geli.warehouse.service;

import com.geli.warehouse.dto.request.auth.LoginRequest;
import com.geli.warehouse.dto.request.auth.RegisterRequest;
import com.geli.warehouse.dto.response.auth.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
