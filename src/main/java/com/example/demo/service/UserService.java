package com.example.demo.service;

import com.example.demo.dto.UserCreateRequest;
import com.example.demo.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface UserService {

    Page<UserResponse> getAllUsers(String email, Pageable pageable);
    UserResponse createUser(UserCreateRequest request);
    UserResponse updateUserRoles(Long userId, Set<String> newRoles);
    void deleteUser(Long userId);
}
