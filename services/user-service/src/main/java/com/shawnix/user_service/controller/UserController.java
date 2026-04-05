package com.shawnix.user_service.controller;

import com.shawnix.user_service.dto.SignupRequest;
import com.shawnix.user_service.dto.LoginRequest;
import com.shawnix.user_service.dto.LoginResponse;
import com.shawnix.user_service.dto.UpdateUserRequest;
import com.shawnix.user_service.dto.UserResponse;
import com.shawnix.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * Signup: Tạo tài khoản mới
     * POST /users/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@RequestBody SignupRequest request) {
        try {
            UserResponse user = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Login: Xác thực người dùng
     * POST /users/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.authenticateUser(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    /**
     * Get Profile: Lấy thông tin profile của user
     * GET /users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable UUID id) {
        try {
            UserResponse user = userService.getUserProfile(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update Profile: Cập nhật thông tin profile của user
     * PUT /users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUserProfile(
        @PathVariable UUID id,
        @RequestBody UpdateUserRequest request
    ) {
        try {
            UserResponse user = userService.updateUserProfile(id, request);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
