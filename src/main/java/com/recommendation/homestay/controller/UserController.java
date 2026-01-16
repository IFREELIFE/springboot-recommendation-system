package com.recommendation.homestay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.recommendation.homestay.config.UploadUtils;
import com.recommendation.homestay.dto.ApiResponse;
import com.recommendation.homestay.dto.UpdateUserRequest;
import com.recommendation.homestay.entity.User;
import com.recommendation.homestay.mapper.UserMapper;
import com.recommendation.homestay.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("isAuthenticated()")
@Tag(name = "User", description = "用户个人信息接口")
public class
UserController {

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp"));
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取当前登录用户的个人资料。
     * @param currentUser
     * @return
     */
    @GetMapping("/me")
    @Operation(summary = "获取个人信息", description = "返回当前登录用户的个人资料")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        User user = userMapper.selectById(currentUser.getId());
        if (user == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "未找到用户"));
        }
        user.setPassword(null);
        return ResponseEntity.ok(new ApiResponse(true, "获取个人信息成功", user));
    }

    /**
     * 上传用户头像，沿用房源图片上传的校验与存储策略。
     * @param file 头像图片文件
     * @return 上传结果，成功时返回可直接访问的头像 URL
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传头像", description = "上传头像图片并返回可访问的URL")
    public ResponseEntity<?> uploadAvatar(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "请上传头像文件"));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "文件名无效"));
        }
        originalFilename = StringUtils.cleanPath(originalFilename);

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex != -1) {
            extension = originalFilename.substring(dotIndex);
        }
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "仅支持上传图片格式：" + ALLOWED_EXTENSIONS));
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "文件大小不能超过10MB"));
        }

        try {
            Path uploadDir = UploadUtils.getUploadDir().normalize();
            try {
                Files.createDirectories(uploadDir);
            } catch (SecurityException se) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(false, "无法创建上传目录"));
            }

            String filename = UUID.randomUUID().toString() + extension;
            Path targetPath = uploadDir.resolve(filename).normalize();
            if (!targetPath.startsWith(uploadDir)) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "文件路径无效"));
            }

            file.transferTo(targetPath.toFile());
            String url = "/api/uploads/" + filename;
            return ResponseEntity.ok(new ApiResponse(true, "头像上传成功", url));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "头像上传失败，请稍后重试"));
        }
    }

    /**
     * 更新当前登录用户的基本资料与密码。
     * @param request
     * @param currentUser
     * @return
     */
    @PutMapping("/me")
    @Operation(summary = "更新个人信息", description = "修改当前登录用户的基本资料与密码")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        User user = userMapper.selectById(currentUser.getId());
        if (user == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "未找到用户"));
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            QueryWrapper<User> emailQuery = new QueryWrapper<>();
            emailQuery.eq("email", request.getEmail()).ne("id", user.getId());
            // 数据库应保持唯一约束，这里先显式检查以提供友好错误信息，实际唯一性仍以数据库约束兜底避免并发竞态
            if (userMapper.selectCount(emailQuery) > 0) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "邮箱已被使用"));
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
        return ResponseEntity.ok(new ApiResponse(true, "个人信息更新成功", user));
    }
}
