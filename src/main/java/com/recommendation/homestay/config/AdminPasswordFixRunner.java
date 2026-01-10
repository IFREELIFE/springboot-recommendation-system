package com.recommendation.homestay.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.recommendation.homestay.entity.User;
import com.recommendation.homestay.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 修复历史种子数据中管理员密码哈希不正确的问题。
 */
@Component
public class AdminPasswordFixRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminPasswordFixRunner.class);
    private static final String LEGACY_PLACEHOLDER_HASH =
            "$2a$10$xqTzp7Z5q7Z5q7Z5q7Z5qeN8qK5R5q7Z5q7Z5q7Z5q7Z5q7Z5q7Zu";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final String defaultAdminPassword;

    public AdminPasswordFixRunner(UserMapper userMapper,
                                  PasswordEncoder passwordEncoder,
                                  @Value("${admin.default.password:admin123}") String defaultAdminPassword) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.defaultAdminPassword = defaultAdminPassword;
    }

    @Override
    public void run(String... args) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", "admin");
        User admin = userMapper.selectOne(queryWrapper);

        if (admin != null && LEGACY_PLACEHOLDER_HASH.equals(admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            userMapper.updateById(admin);
            log.info("Admin password hash was updated from legacy placeholder.");
        }
    }
}
