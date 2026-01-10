package com.recommendation.homestay.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.recommendation.homestay.entity.User;
import com.recommendation.homestay.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 修复历史种子数据中管理员密码哈希不正确的问题。
 */
@Component
public class AdminPasswordFixRunner implements CommandLineRunner {

    private static final String LEGACY_PLACEHOLDER_HASH =
            "$2a$10$xqTzp7Z5q7Z5q7Z5q7Z5qeN8qK5R5q7Z5q7Z5q7Z5q7Z5q7Z5q7Zu";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminPasswordFixRunner(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", "admin");
        User admin = userMapper.selectOne(queryWrapper);

        if (admin != null && LEGACY_PLACEHOLDER_HASH.equals(admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
            userMapper.updateById(admin);
        }
    }
}
