package com.recommendation.homestay.controller;

import com.recommendation.homestay.dto.ApiResponse;
import com.recommendation.homestay.dto.JwtResponse;
import com.recommendation.homestay.dto.LoginRequest;
import com.recommendation.homestay.dto.RegisterRequest;
import com.recommendation.homestay.entity.User;
import com.recommendation.homestay.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Auth", description = "用户注册与登录接口")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 注册新用户账户，返回生成的用户ID。
     */
    @PostMapping("/register")
    @Operation(summary = "注册用户", description = "创建新用户账户并返回用户ID")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "用户注册成功", user.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    /**
     * 校验用户凭证并返回 JWT 令牌。
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "校验凭证并返回JWT令牌")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request) {
        try {
            JwtResponse jwtResponse = authService.loginUser(request);
            return ResponseEntity.ok(new ApiResponse(true, "登录成功", jwtResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "用户名或密码无效"));
        }
    }
}
