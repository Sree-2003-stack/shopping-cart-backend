package com.securecart.backend.service;

import com.securecart.backend.dto.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
}
