-- Admin specific schema additions for Homestay Recommendation System
USE homestay_recommendation;

-- 管理员账户表：为管理员端预留独立资料与状态管理
CREATE TABLE IF NOT EXISTS admin_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    display_name VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_admin_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 将现有管理员用户写入管理员表（若已存在则忽略）
INSERT IGNORE INTO admin_accounts (user_id, display_name, status)
SELECT id, username, 'ACTIVE'
FROM users
WHERE role = 'ADMIN'
LIMIT 1;
