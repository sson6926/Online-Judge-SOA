package com.shawnix.user_service.service;

import com.shawnix.user_service.dto.SignupRequest;
import com.shawnix.user_service.dto.UserResponse;
import com.shawnix.user_service.entity.User;
import com.shawnix.user_service.entity.UserRole;
import com.shawnix.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Tạo tài khoản người dùng mới (Signup)
     */
    public UserResponse createUser(SignupRequest request) {
        // Check duplicate username
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check duplicate email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.STUDENT);  // Default role
        
        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }
    
    /**
     * Helper: Convert User entity to UserResponse DTO
     */
    private UserResponse mapToResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
