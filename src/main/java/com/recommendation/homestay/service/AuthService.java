package com.recommendation.homestay.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.recommendation.homestay.dto.JwtResponse;
import com.recommendation.homestay.dto.LoginRequest;
import com.recommendation.homestay.dto.RegisterRequest;
import com.recommendation.homestay.entity.User;
import com.recommendation.homestay.mapper.UserMapper;
import com.recommendation.homestay.security.JwtTokenProvider;
import com.recommendation.homestay.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service
 * 
 * Handles user authentication and registration operations using MyBatis-Plus.
 * Provides secure user management with password encryption and JWT token generation.
 * 
 * Key features:
 * - User registration with duplicate validation
 * - Secure login with JWT token generation
 * - Password encryption using BCrypt
 * - Transaction management for data consistency
 * 
 * @author Homestay Recommendation System
 */
@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Register a new user
     * 
     * Validates that username and email are unique, encrypts password,
     * and creates new user record using MyBatis-Plus insert operation.
     * 
     * @param request Registration request containing user details
     * @return Created user entity
     * @throws RuntimeException if username or email already exists
     */
    @Transactional
    public User registerUser(RegisterRequest request) {
        // Check username uniqueness using MyBatis-Plus QueryWrapper
        QueryWrapper<User> usernameQuery = new QueryWrapper<>();
        usernameQuery.eq("username", request.getUsername());
        if (userMapper.selectCount(usernameQuery) > 0) {
            throw new RuntimeException("Username already exists");
        }

        // Check email uniqueness using MyBatis-Plus QueryWrapper
        QueryWrapper<User> emailQuery = new QueryWrapper<>();
        emailQuery.eq("email", request.getEmail());
        if (userMapper.selectCount(emailQuery) > 0) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user with encrypted password
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        
        // Set user role from request or default to USER
        if (request.getRole() != null) {
            try {
                user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                user.setRole(User.Role.USER);
            }
        } else {
            user.setRole(User.Role.USER);
        }
        
        user.setEnabled(true);

        // Insert using MyBatis-Plus
        userMapper.insert(user);
        return user;
    }

    /**
     * Authenticate user and generate JWT token
     * 
     * Validates credentials and returns JWT token for authenticated access.
     * 
     * @param request Login request with username and password
     * @return JWT response containing token and user details
     */
    public JwtResponse loginUser(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        return new JwtResponse(
            jwt,
            userPrincipal.getId(),
            userPrincipal.getUsername(),
            userPrincipal.getEmail(),
            userPrincipal.getAuthorities().iterator().next().getAuthority()
        );
    }
}
