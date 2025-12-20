-- Sample Data for Homestay Recommendation System

USE homestay_recommendation;

-- Sample Landlords
INSERT INTO users (username, email, password, phone, role, enabled) VALUES 
('landlord_zhang', 'zhang@homestay.com', '$2a$10$xqTzp7Z5q7Z5q7Z5q7Z5qeN8qK5R5q7Z5q7Z5q7Z5q7Z5q7Z5q7Zu', '13800138001', 'LANDLORD', TRUE),
('landlord_wang', 'wang@homestay.com', '$2a$10$xqTzp7Z5q7Z5q7Z5q7Z5qeN8qK5R5q7Z5q7Z5q7Z5q7Z5q7Z5q7Zu', '13800138002', 'LANDLORD', TRUE),
('landlord_li', 'li@homestay.com', '$2a$10$xqTzp7Z5q7Z5q7Z5q7Z5qeN8qK5R5q7Z5q7Z5q7Z5q7Z5q7Z5q7Zu', '13800138003', 'LANDLORD', TRUE)
ON DUPLICATE KEY UPDATE username=username;

-- Sample Users
INSERT INTO users (username, email, password, phone, role, enabled) VALUES 
('user_chen', 'chen@example.com', '$2a$10$xqTzp7Z5q7Z5q7Z5q7Z5qeN8qK5R5q7Z5q7Z5q7Z5q7Z5q7Z5q7Zu', '13900139001', 'USER', TRUE),
('user_liu', 'liu@example.com', '$2a$10$xqTzp7Z5q7Z5q7Z5q7Z5qeN8qK5R5q7Z5q7Z5q7Z5q7Z5q7Z5q7Zu', '13900139002', 'USER', TRUE)
ON DUPLICATE KEY UPDATE username=username;

-- Sample Properties
INSERT INTO properties (title, description, city, district, address, price, bedrooms, bathrooms, max_guests, property_type, amenities, images, available, landlord_id, rating, review_count, view_count, booking_count) VALUES 
('Cozy Downtown Apartment', 'Modern apartment in the heart of the city with great amenities', 'Beijing', 'Chaoyang', '123 Chaoyang Road', 500.00, 2, 1, 4, 'apartment', '["WiFi","Air Conditioning","Kitchen","Washer"]', '["image1.jpg","image2.jpg"]', TRUE, 2, 4.5, 20, 150, 35),
('Luxury Villa with Garden', 'Spacious villa with beautiful garden and pool', 'Shanghai', 'Pudong', '456 Pudong Avenue', 1200.00, 4, 3, 8, 'villa', '["WiFi","Air Conditioning","Pool","Garden","Parking"]', '["villa1.jpg","villa2.jpg","villa3.jpg"]', TRUE, 2, 4.8, 15, 200, 28),
('Traditional Courtyard House', 'Authentic Beijing courtyard house with modern comfort', 'Beijing', 'Dongcheng', '789 Gulou Street', 800.00, 3, 2, 6, 'house', '["WiFi","Air Conditioning","Traditional Decor","Kitchen"]', '["house1.jpg","house2.jpg"]', TRUE, 3, 4.6, 12, 180, 22),
('Seaside Studio', 'Comfortable studio with ocean view', 'Qingdao', 'Shinan', '321 Beach Road', 350.00, 1, 1, 2, 'apartment', '["WiFi","Sea View","Kitchen"]', '["studio1.jpg"]', TRUE, 3, 4.3, 8, 95, 18),
('Mountain Retreat Cabin', 'Peaceful cabin in the mountains, perfect for nature lovers', 'Chengdu', 'Dujiangyan', '555 Mountain Path', 600.00, 2, 1, 4, 'house', '["WiFi","Fireplace","Mountain View","Hiking"]', '["cabin1.jpg","cabin2.jpg"]', TRUE, 4, 4.7, 18, 120, 25),
('City Center Loft', 'Modern loft in downtown area with metro access', 'Guangzhou', 'Tianhe', '888 Tianhe Road', 700.00, 2, 2, 4, 'apartment', '["WiFi","Air Conditioning","Metro Access","Gym"]', '["loft1.jpg","loft2.jpg"]', TRUE, 4, 4.4, 10, 140, 20)
ON DUPLICATE KEY UPDATE title=title;

-- Sample Orders
INSERT INTO orders (order_number, user_id, property_id, check_in_date, check_out_date, guest_count, total_price, status) VALUES 
('ORD-12345678', 5, 1, '2024-12-25', '2024-12-28', 2, 1500.00, 'CONFIRMED'),
('ORD-87654321', 6, 2, '2025-01-10', '2025-01-15', 4, 6000.00, 'PENDING'),
('ORD-11223344', 5, 3, '2024-12-20', '2024-12-23', 3, 2400.00, 'COMPLETED')
ON DUPLICATE KEY UPDATE order_number=order_number;

-- Sample User Interactions (for recommendation algorithm)
INSERT INTO user_property_interactions (user_id, property_id, type, rating) VALUES 
(5, 1, 'VIEW', NULL),
(5, 1, 'BOOK', NULL),
(5, 1, 'REVIEW', 5),
(5, 2, 'VIEW', NULL),
(5, 3, 'BOOK', NULL),
(5, 3, 'REVIEW', 4),
(6, 2, 'VIEW', NULL),
(6, 2, 'FAVORITE', NULL),
(6, 4, 'VIEW', NULL),
(6, 5, 'VIEW', NULL)
ON DUPLICATE KEY UPDATE id=id;
