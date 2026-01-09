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
 * 认证服务
 *
 * 使用 MyBatis-Plus 处理用户认证与注册逻辑，提供密码加密与 JWT 令牌生成等安全能力。
 *
 * 主要功能：
 * - 用户注册时校验重复
 * - 生成 JWT 令牌的安全登录
 * - 通过 BCrypt 加密密码
 * - 事务管理保证数据一致性
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
     * 注册新用户
     *
     * 校验用户名与邮箱唯一性，加密密码并通过 MyBatis-Plus 插入用户记录。
     *
     * @param request 包含用户信息的注册请求
     * @return 新建的用户实体
     * @throws RuntimeException 当用户名或邮箱已存在时抛出
     */
    @Transactional
    public User registerUser(RegisterRequest request) {
        // 使用 MyBatis-Plus QueryWrapper 校验用户名唯一性
        QueryWrapper<User> usernameQuery = new QueryWrapper<>();
        usernameQuery.eq("username", request.getUsername());
        if (userMapper.selectCount(usernameQuery) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 使用 MyBatis-Plus QueryWrapper 校验邮箱唯一性
        QueryWrapper<User> emailQuery = new QueryWrapper<>();
        emailQuery.eq("email", request.getEmail());
        if (userMapper.selectCount(emailQuery) > 0) {
            throw new RuntimeException("邮箱已存在");
        }

        // 创建用户并加密密码
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        
        // 从请求中设置角色，默认 USER
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

        // 通过 MyBatis-Plus 插入数据
        userMapper.insert(user);
        return user;
    }

    /**
     * 认证用户并生成 JWT 令牌
     *
     * 校验凭证后返回包含令牌与用户信息的响应。
     *
     * @param request 携带用户名与密码的登录请求
     * @return 包含令牌和用户详情的 JWT 响应
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
