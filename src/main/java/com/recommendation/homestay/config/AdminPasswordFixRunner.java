package com.recommendation.homestay.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.recommendation.homestay.entity.User;
import com.recommendation.homestay.mapper.UserMapper;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Fixes the incorrect admin password hash left by legacy seed data.
 */
@Component
public class AdminPasswordFixRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminPasswordFixRunner.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final String defaultAdminPassword;
    private final String legacyPlaceholderHash;
    private final boolean legacyFixEnabled;

    public AdminPasswordFixRunner(UserMapper userMapper,
                                  PasswordEncoder passwordEncoder,
                                  @Value("${admin.default.password:admin123}") String defaultAdminPassword,
                                  // Default matches the incorrect placeholder bcrypt value shipped in earlier seed data
                                  @Value("${admin.legacy.placeholder-hash:$2a$10$xqTzp7Z5q7Z5q7Z5q7Z5qeN8qK5R5q7Z5q7Z5q7Z5q7Z5q7Z5q7Zu}")
                                  String legacyPlaceholderHash,
                                  @Value("${admin.legacy.fix-enabled:true}") boolean legacyFixEnabled) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.defaultAdminPassword = defaultAdminPassword;
        this.legacyPlaceholderHash = legacyPlaceholderHash;
        this.legacyFixEnabled = legacyFixEnabled;
    }

    /**
     * Executes at application startup to replace the legacy admin password hash if it is still present.
     */
    @Override
    public void run(String... args) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", "admin");
        User admin = userMapper.selectOne(queryWrapper);

        if (legacyFixEnabled && admin != null && Objects.equals(admin.getPassword(), legacyPlaceholderHash)) {
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            int updated = userMapper.updateById(admin);
            if (updated > 0) {
                log.info("Admin password hash was updated from legacy placeholder.");
            } else {
                log.warn("Failed to update admin password hash from legacy placeholder.");
            }
        }
    }
}
