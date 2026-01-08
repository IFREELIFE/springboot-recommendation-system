package com.recommendation.homestay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.recommendation.homestay.dto.ApiResponse;
import com.recommendation.homestay.dto.UpdateUserRequest;
import com.recommendation.homestay.entity.User;
import com.recommendation.homestay.mapper.UserMapper;
import com.recommendation.homestay.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("isAuthenticated()")
@Tag(name = "User", description = "用户个人信息接口")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    @Operation(summary = "获取个人信息", description = "返回当前登录用户的个人资料")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        User user = userMapper.selectById(currentUser.getId());
        if (user == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "User not found"));
        }
        user.setPassword(null);
        return ResponseEntity.ok(new ApiResponse(true, "Profile retrieved successfully", user));
    }

    @PutMapping("/me")
    @Operation(summary = "更新个人信息", description = "修改当前登录用户的基本资料与密码")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        User user = userMapper.selectById(currentUser.getId());
        if (user == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "User not found"));
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            QueryWrapper<User> emailQuery = new QueryWrapper<>();
            emailQuery.eq("email", request.getEmail()).ne("id", user.getId());
            // 数据库应保持唯一约束，这里先显式检查以提供友好错误信息，实际唯一性仍以数据库约束兜底避免并发竞态
            if (userMapper.selectCount(emailQuery) > 0) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Email already in use"));
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userMapper.updateById(user);
        user.setPassword(null);
        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully", user));
    }
}
