-- MySQL Database Schema for Homestay Recommendation System
-- Created: 2024

-- Create database
CREATE DATABASE IF NOT EXISTS homestay_recommendation 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE homestay_recommendation;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    avatar VARCHAR(200),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Properties Table
CREATE TABLE IF NOT EXISTS properties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    city VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    bedrooms INT NOT NULL,
    bathrooms INT NOT NULL,
    max_guests INT NOT NULL,
    property_type VARCHAR(50),
    amenities TEXT,
    images TEXT,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    landlord_id BIGINT NOT NULL,
    rating DECIMAL(3, 2) DEFAULT 0.00,
    review_count INT DEFAULT 0,
    view_count INT DEFAULT 0,
    booking_count INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (landlord_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_city (city),
    INDEX idx_landlord (landlord_id),
    INDEX idx_available (available),
    INDEX idx_price (price),
    INDEX idx_rating (rating),
    INDEX idx_booking_count (booking_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Orders Table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    property_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    guest_count INT NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    INDEX idx_order_number (order_number),
    INDEX idx_user (user_id),
    INDEX idx_property (property_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- User Property Interactions Table (for recommendation algorithm)
CREATE TABLE IF NOT EXISTS user_property_interactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    property_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    rating INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_property (property_id),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample admin user (password: admin123)
INSERT INTO users (username, email, password, role, enabled) VALUES 
('admin', 'admin@homestay.com', '$2a$10$xqTzp7Z5q7Z5q7Z5q7Z5qeN8qK5R5q7Z5q7Z5q7Z5q7Z5q7Z5q7Zu', 'ADMIN', TRUE)
ON DUPLICATE KEY UPDATE username=username;

-- Insert sample landlord user (password: landlord123)
INSERT INTO users (username, email, password, role, enabled) VALUES 
('landlord1', 'landlord1@homestay.com', '$2a$10$xqTzp7Z5q7Z5q7Z5q7Z5qeN8qK5R5q7Z5q7Z5q7Z5q7Z5q7Z5q7Zu', 'LANDLORD', TRUE)
ON DUPLICATE KEY UPDATE username=username;

-- Insert sample regular user (password: user123)
INSERT INTO users (username, email, password, role, enabled) VALUES 
('user1', 'user1@homestay.com', '$2a$10$xqTzp7Z5q7Z5q7Z5q7Z5qeN8qK5R5q7Z5q7Z5q7Z5q7Z5q7Z5q7Zu', 'USER', TRUE)
ON DUPLICATE KEY UPDATE username=username;
