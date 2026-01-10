package com.recommendation.homestay.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recommendation.homestay.dto.AdminAccountDTO;
import com.recommendation.homestay.dto.ApiResponse;
import com.recommendation.homestay.dto.PageResponse;
import com.recommendation.homestay.dto.PropertyOccupancyDTO;
import com.recommendation.homestay.entity.User;
import com.recommendation.homestay.mapper.UserMapper;
import com.recommendation.homestay.security.UserPrincipal;
import com.recommendation.homestay.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin", description = "管理员端接口")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PropertyService propertyService;

    @GetMapping("/users")
    @Operation(summary = "用户与房东列表", description = "按角色筛选并查看所有账户状态")
    public ResponseEntity<?> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<User> pageParam = new Page<>(page + 1, size);
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            if (StringUtils.hasText(role)) {
                try {
                    User.Role parsedRole = User.Role.valueOf(role.toUpperCase());
                    queryWrapper.eq("role", parsedRole.name());
                } catch (IllegalArgumentException ex) {
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse(false, "角色参数无效"));
                }
            }

            IPage<User> users = userMapper.selectPage(pageParam, queryWrapper);
            PageResponse<AdminAccountDTO> response = toAccountPage(users);
            return ResponseEntity.ok(new ApiResponse(true, "账户列表获取成功", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "获取账户列表失败"));
        }
    }

    @PutMapping("/users/{id}/freeze")
    @Operation(summary = "冻结或解冻账户", description = "管理员可对用户与房东账户进行冻结或解冻")
    @Transactional
    public ResponseEntity<?> freezeAccount(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean freeze,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "未找到用户"));
        }
        if (currentUser != null && currentUser.getId().equals(id)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "不能冻结当前登录的管理员账号"));
        }
        if (user.getRole() == User.Role.ADMIN) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "管理员账号不可冻结"));
        }
        user.setEnabled(!freeze);
        userMapper.updateById(user);
        String message = freeze ? "账户已冻结" : "账户已解冻";
        return ResponseEntity.ok(new ApiResponse(true, message, toAccountDTO(user)));
    }

    @GetMapping("/properties/occupancy")
    @Operation(summary = "房源入住情况", description = "查看所有房源的入住与剩余情况，可按房东筛选")
    public ResponseEntity<?> getPropertyOccupancy(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long landlordId) {
        try {
            PageResponse<PropertyOccupancyDTO> response = propertyService.getPropertyOccupancyForAdmin(landlordId, page, size);
            return ResponseEntity.ok(new ApiResponse(true, "房源入住情况获取成功", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "获取房源入住情况失败"));
        }
    }

    private PageResponse<AdminAccountDTO> toAccountPage(IPage<User> users) {
        List<AdminAccountDTO> records = users.getRecords().stream()
                .map(this::toAccountDTO)
                .collect(Collectors.toList());
        PageResponse<AdminAccountDTO> response = new PageResponse<>();
        response.setRecords(records);
        response.setTotal(users.getTotal());
        response.setSize(users.getSize());
        response.setCurrent(users.getCurrent());
        response.setPages(users.getPages());
        return response;
    }

    private AdminAccountDTO toAccountDTO(User user) {
        AdminAccountDTO dto = new AdminAccountDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setEnabled(user.getEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
