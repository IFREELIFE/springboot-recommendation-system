package com.recommendation.homestay.service;

import com.recommendation.homestay.dto.JwtResponse;
import com.recommendation.homestay.dto.LoginRequest;
import com.recommendation.homestay.dto.RegisterRequest;
import com.recommendation.homestay.entity.User;
import com.recommendation.homestay.repository.UserRepository;
import com.recommendation.homestay.security.JwtTokenProvider;
import com.recommendation.homestay.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Transactional
    public User registerUser(RegisterRequest request) {
        logger.info("Registering new user: {}", request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            logger.error("Registration failed: Username already exists: {}", request.getUsername());
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            logger.error("Registration failed: Email already exists: {}", request.getEmail());
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        
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

        User savedUser = userRepository.save(user);
        logger.info("Successfully registered user: {}, userId: {}", savedUser.getUsername(), savedUser.getId());
        return savedUser;
    }

    public JwtResponse loginUser(LoginRequest request) {
        logger.info("Login attempt for user: {}", request.getUsername());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            logger.info("Login successful for user: {}, userId: {}", 
                       userPrincipal.getUsername(), userPrincipal.getId());

            return new JwtResponse(
                jwt,
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                userPrincipal.getEmail(),
                userPrincipal.getAuthorities().iterator().next().getAuthority()
            );
        } catch (Exception e) {
            logger.error("Login failed for user: {}, error: {}", request.getUsername(), e.getMessage());
            throw e;
        }
    }
}
