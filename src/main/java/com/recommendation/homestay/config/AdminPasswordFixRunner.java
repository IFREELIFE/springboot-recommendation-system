package com.recommendation.homestay.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.recommendation.homestay.entity.User;
import com.recommendation.homestay.mapper.UserMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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
                                  // Default intended for local/dev environments; override in production via configuration
                                  @Value("${admin.default.password:admin123}") String defaultAdminPassword,
                                  // Default matches the incorrect, repeating placeholder bcrypt value shipped in earlier seed data and must stay in sync for compatibility
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
     * The operation is idempotent so concurrent starts simply rewrite the same value once.
     */
    @Override
    public void run(String... args) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", "admin");
        User admin = userMapper.selectOne(queryWrapper);

        if (legacyFixEnabled && admin != null && isLegacyHash(admin.getPassword())) {
            String encodedPassword = passwordEncoder.encode(defaultAdminPassword);
            UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", admin.getId()).eq("password", legacyPlaceholderHash)
                    .set("password", encodedPassword);
            // Conditional update ensures concurrent starters only update while the legacy hash is still present.
            int updated = userMapper.update(null, updateWrapper);
            if (updated > 0) {
                log.info("Admin password hash was updated from legacy placeholder.");
            } else {
                log.warn("Failed to update admin password hash from legacy placeholder.");
            }
        }
    }

    private boolean isLegacyHash(String currentPassword) {
        if (currentPassword == null) {
            return false;
        }
        return MessageDigest.isEqual(
                currentPassword.getBytes(StandardCharsets.UTF_8),
                legacyPlaceholderHash.getBytes(StandardCharsets.UTF_8));
    }
}
