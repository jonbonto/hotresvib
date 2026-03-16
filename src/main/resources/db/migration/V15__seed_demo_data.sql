-- V15: Database seeding with demo data
-- This migration seeds realistic demo data for development and testing

-- ===================== USERS =====================
-- Password for all users: Password1! (bcrypt hash with strength=12)
-- $2a$12$LJ3m4ys3Gp3lN0GJIYkWheZ4k79FXcv0ybw8FXO/TZTHwX5sO0nXi

-- Admin user
INSERT INTO users (id, email, display_name, role, password_hash, failed_login_attempts, locked_until, timezone, marketing_opt_in)
VALUES ('a0000000-0000-0000-0000-000000000001', 'admin@hotresvib.com', 'Admin User', 'ADMIN',
        '$2a$12$LJ3m4ys3Gp3lN0GJIYkWheZ4k79FXcv0ybw8FXO/TZTHwX5sO0nXi', 0, NULL, 'UTC', true)
ON CONFLICT (id) DO NOTHING;

-- Hotel Manager 1
INSERT INTO users (id, email, display_name, role, password_hash, failed_login_attempts, locked_until, timezone, marketing_opt_in)
VALUES ('a0000000-0000-0000-0000-000000000002', 'manager1@hotresvib.com', 'Sarah Johnson', 'STAFF',
        '$2a$12$LJ3m4ys3Gp3lN0GJIYkWheZ4k79FXcv0ybw8FXO/TZTHwX5sO0nXi', 0, NULL, 'America/New_York', true)
ON CONFLICT (id) DO NOTHING;

-- Hotel Manager 2
INSERT INTO users (id, email, display_name, role, password_hash, failed_login_attempts, locked_until, timezone, marketing_opt_in)
VALUES ('a0000000-0000-0000-0000-000000000003', 'manager2@hotresvib.com', 'Michael Chen', 'STAFF',
        '$2a$12$LJ3m4ys3Gp3lN0GJIYkWheZ4k79FXcv0ybw8FXO/TZTHwX5sO0nXi', 0, NULL, 'Asia/Tokyo', true)
ON CONFLICT (id) DO NOTHING;

-- Guest 1
INSERT INTO users (id, email, display_name, role, password_hash, failed_login_attempts, locked_until, timezone, marketing_opt_in)
VALUES ('a0000000-0000-0000-0000-000000000004', 'guest1@example.com', 'Alice Williams', 'CUSTOMER',
        '$2a$12$LJ3m4ys3Gp3lN0GJIYkWheZ4k79FXcv0ybw8FXO/TZTHwX5sO0nXi', 0, NULL, 'Europe/London', true)
ON CONFLICT (id) DO NOTHING;

-- Guest 2
INSERT INTO users (id, email, display_name, role, password_hash, failed_login_attempts, locked_until, timezone, marketing_opt_in)
VALUES ('a0000000-0000-0000-0000-000000000005', 'guest2@example.com', 'Bob Martinez', 'CUSTOMER',
        '$2a$12$LJ3m4ys3Gp3lN0GJIYkWheZ4k79FXcv0ybw8FXO/TZTHwX5sO0nXi', 0, NULL, 'America/Los_Angeles', true)
ON CONFLICT (id) DO NOTHING;

-- Guest 3
INSERT INTO users (id, email, display_name, role, password_hash, failed_login_attempts, locked_until, timezone, marketing_opt_in)
VALUES ('a0000000-0000-0000-0000-000000000006', 'guest3@example.com', 'Carol Davis', 'CUSTOMER',
        '$2a$12$LJ3m4ys3Gp3lN0GJIYkWheZ4k79FXcv0ybw8FXO/TZTHwX5sO0nXi', 0, NULL, 'Europe/Paris', true)
ON CONFLICT (id) DO NOTHING;

-- Guest 4
INSERT INTO users (id, email, display_name, role, password_hash, failed_login_attempts, locked_until, timezone, marketing_opt_in)
VALUES ('a0000000-0000-0000-0000-000000000007', 'guest4@example.com', 'David Lee', 'CUSTOMER',
        '$2a$12$LJ3m4ys3Gp3lN0GJIYkWheZ4k79FXcv0ybw8FXO/TZTHwX5sO0nXi', 0, NULL, 'Asia/Singapore', true)
ON CONFLICT (id) DO NOTHING;

-- Guest 5
INSERT INTO users (id, email, display_name, role, password_hash, failed_login_attempts, locked_until, timezone, marketing_opt_in)
VALUES ('a0000000-0000-0000-0000-000000000008', 'guest5@example.com', 'Emma Brown', 'CUSTOMER',
        '$2a$12$LJ3m4ys3Gp3lN0GJIYkWheZ4k79FXcv0ybw8FXO/TZTHwX5sO0nXi', 0, NULL, 'Australia/Sydney', true)
ON CONFLICT (id) DO NOTHING;

-- ===================== HOTELS =====================

-- Hotel 1: Grand Palace Hotel (Paris)
INSERT INTO hotels (id, name, city, country, is_featured, description, address, phone, email, star_rating, image_url)
VALUES ('b0000000-0000-0000-0000-000000000001', 'Grand Palace Hotel', 'Paris', 'France', true,
        'Experience luxury in the heart of Paris. Our 5-star hotel offers stunning views of the Eiffel Tower, world-class dining, and impeccable service.',
        '15 Avenue des Champs-Élysées, 75008 Paris', '+33-1-4567-8900', 'info@grandpalace.com', 5,
        'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800')
ON CONFLICT (id) DO NOTHING;

-- Hotel 2: Tokyo Skyline Inn (Tokyo)
INSERT INTO hotels (id, name, city, country, is_featured, description, address, phone, email, star_rating, image_url)
VALUES ('b0000000-0000-0000-0000-000000000002', 'Tokyo Skyline Inn', 'Tokyo', 'Japan', true,
        'Modern comfort meets traditional Japanese hospitality. Located in Shinjuku with breathtaking city views and easy access to major attractions.',
        '2-1-1 Nishi-Shinjuku, Shinjuku-ku, Tokyo', '+81-3-1234-5678', 'reservations@tokyoskyline.com', 4,
        'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=800')
ON CONFLICT (id) DO NOTHING;

-- Hotel 3: Manhattan Central Hotel (New York)
INSERT INTO hotels (id, name, city, country, is_featured, description, address, phone, email, star_rating, image_url)
VALUES ('b0000000-0000-0000-0000-000000000003', 'Manhattan Central Hotel', 'New York', 'USA', true,
        'Prime location in Midtown Manhattan. Steps from Times Square, Broadway theaters, and Central Park. Modern rooms with city skyline views.',
        '234 West 42nd Street, New York, NY 10036', '+1-212-555-0100', 'stay@manhattancentral.com', 4,
        'https://images.unsplash.com/photo-1455587734955-081b22074882?w=800')
ON CONFLICT (id) DO NOTHING;

-- Hotel 4: Bali Serenity Resort (Bali)
INSERT INTO hotels (id, name, city, country, is_featured, description, address, phone, email, star_rating, image_url)
VALUES ('b0000000-0000-0000-0000-000000000004', 'Bali Serenity Resort', 'Bali', 'Indonesia', false,
        'A tropical paradise retreat with private villas, infinity pools, and lush gardens. Perfect for romantic getaways and wellness retreats.',
        'Jl. Raya Ubud No. 88, Ubud, Bali 80571', '+62-361-123456', 'hello@baliserenity.com', 5,
        'https://images.unsplash.com/photo-1571896349842-33c89424de2d?w=800')
ON CONFLICT (id) DO NOTHING;

-- ===================== ROOMS =====================

-- Grand Palace Hotel rooms
INSERT INTO rooms (id, hotel_id, number, type, base_rate_amount, base_rate_currency, description, capacity, amenities, image_url)
VALUES 
('c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', '101', 'SINGLE', 150.00, 'USD',
 'Cozy single room with elegant Parisian decor and garden view.', 1, 'WiFi,TV,Mini Bar,Safe,Air Conditioning', NULL),
('c0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001', '201', 'DOUBLE', 250.00, 'USD',
 'Spacious double room with king-size bed and partial Eiffel Tower view.', 2, 'WiFi,TV,Mini Bar,Safe,Air Conditioning,Room Service,Balcony', NULL),
('c0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000001', '301', 'SUITE', 500.00, 'USD',
 'Presidential suite with panoramic views, separate living area, and luxury amenities.', 4, 'WiFi,TV,Mini Bar,Safe,Air Conditioning,Room Service,Balcony,Jacuzzi,Butler Service', NULL),
('c0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000001', '102', 'SINGLE', 150.00, 'USD',
 'Classic single room with courtyard view.', 1, 'WiFi,TV,Mini Bar,Safe,Air Conditioning', NULL),
('c0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000001', '202', 'DOUBLE', 275.00, 'USD',
 'Deluxe double room with direct Eiffel Tower view.', 2, 'WiFi,TV,Mini Bar,Safe,Air Conditioning,Room Service,Balcony', NULL)
ON CONFLICT (id) DO NOTHING;

-- Tokyo Skyline Inn rooms
INSERT INTO rooms (id, hotel_id, number, type, base_rate_amount, base_rate_currency, description, capacity, amenities, image_url)
VALUES 
('c0000000-0000-0000-0000-000000000006', 'b0000000-0000-0000-0000-000000000002', '1001', 'SINGLE', 120.00, 'USD',
 'Modern single room with floor-to-ceiling windows and city view.', 1, 'WiFi,TV,Safe,Air Conditioning,Japanese Bath', NULL),
('c0000000-0000-0000-0000-000000000007', 'b0000000-0000-0000-0000-000000000002', '1101', 'DOUBLE', 200.00, 'USD',
 'Premium double room with Mount Fuji view on clear days.', 2, 'WiFi,TV,Safe,Air Conditioning,Japanese Bath,Mini Bar', NULL),
('c0000000-0000-0000-0000-000000000008', 'b0000000-0000-0000-0000-000000000002', '2001', 'SUITE', 400.00, 'USD',
 'Executive suite with traditional Japanese tatami area and skyline views.', 4, 'WiFi,TV,Safe,Air Conditioning,Japanese Bath,Mini Bar,Lounge Access,Room Service', NULL),
('c0000000-0000-0000-0000-000000000009', 'b0000000-0000-0000-0000-000000000002', '1002', 'SINGLE', 120.00, 'USD',
 'Compact single room with modern amenities.', 1, 'WiFi,TV,Safe,Air Conditioning', NULL)
ON CONFLICT (id) DO NOTHING;

-- Manhattan Central Hotel rooms
INSERT INTO rooms (id, hotel_id, number, type, base_rate_amount, base_rate_currency, description, capacity, amenities, image_url)
VALUES 
('c0000000-0000-0000-0000-000000000010', 'b0000000-0000-0000-0000-000000000003', '501', 'SINGLE', 180.00, 'USD',
 'Classic Manhattan single with skyline views.', 1, 'WiFi,TV,Safe,Air Conditioning,Coffee Maker', NULL),
('c0000000-0000-0000-0000-000000000011', 'b0000000-0000-0000-0000-000000000003', '601', 'DOUBLE', 300.00, 'USD',
 'Times Square view double room with modern design.', 2, 'WiFi,TV,Safe,Air Conditioning,Coffee Maker,Mini Bar,Room Service', NULL),
('c0000000-0000-0000-0000-000000000012', 'b0000000-0000-0000-0000-000000000003', '801', 'SUITE', 550.00, 'USD',
 'Penthouse suite with wrap-around views of Manhattan skyline.', 4, 'WiFi,TV,Safe,Air Conditioning,Coffee Maker,Mini Bar,Room Service,Kitchenette,Fitness Access', NULL),
('c0000000-0000-0000-0000-000000000013', 'b0000000-0000-0000-0000-000000000003', '502', 'SINGLE', 180.00, 'USD',
 'Compact single room with Central Park proximity.', 1, 'WiFi,TV,Safe,Air Conditioning,Coffee Maker', NULL),
('c0000000-0000-0000-0000-000000000014', 'b0000000-0000-0000-0000-000000000003', '602', 'DOUBLE', 320.00, 'USD',
 'Premium double room with Central Park view.', 2, 'WiFi,TV,Safe,Air Conditioning,Coffee Maker,Mini Bar,Room Service', NULL)
ON CONFLICT (id) DO NOTHING;

-- Bali Serenity Resort rooms
INSERT INTO rooms (id, hotel_id, number, type, base_rate_amount, base_rate_currency, description, capacity, amenities, image_url)
VALUES
('c0000000-0000-0000-0000-000000000015', 'b0000000-0000-0000-0000-000000000004', 'V1', 'DOUBLE', 220.00, 'USD',
 'Garden villa with private terrace and tropical garden view.', 2, 'WiFi,TV,Air Conditioning,Pool Access,Spa Access', NULL),
('c0000000-0000-0000-0000-000000000016', 'b0000000-0000-0000-0000-000000000004', 'V2', 'SUITE', 450.00, 'USD',
 'Pool villa with private infinity pool overlooking rice terraces.', 4, 'WiFi,TV,Air Conditioning,Private Pool,Spa Access,Butler Service,Outdoor Shower', NULL),
('c0000000-0000-0000-0000-000000000017', 'b0000000-0000-0000-0000-000000000004', 'V3', 'DOUBLE', 240.00, 'USD',
 'Jungle villa surrounded by lush tropical vegetation.', 2, 'WiFi,TV,Air Conditioning,Pool Access,Spa Access,Yoga Deck', NULL)
ON CONFLICT (id) DO NOTHING;

-- ===================== PRICING RULES =====================
-- Seasonal pricing for the next 90 days

INSERT INTO pricing_rules (id, room_id, start_date, end_date, price_amount, price_currency, description)
VALUES 
('pr-001', 'c0000000-0000-0000-0000-000000000003', CURRENT_DATE + INTERVAL '30 days', CURRENT_DATE + INTERVAL '60 days', 600.00, 'USD', 'High season rate - Grand Palace Suite'),
('pr-002', 'c0000000-0000-0000-0000-000000000008', CURRENT_DATE + INTERVAL '14 days', CURRENT_DATE + INTERVAL '45 days', 480.00, 'USD', 'Cherry blossom season - Tokyo Suite'),
('pr-003', 'c0000000-0000-0000-0000-000000000012', CURRENT_DATE + INTERVAL '20 days', CURRENT_DATE + INTERVAL '50 days', 650.00, 'USD', 'Holiday season - Manhattan Penthouse')
ON CONFLICT (id) DO NOTHING;

-- ===================== RESERVATIONS =====================

-- Reservation 1: Alice at Grand Palace (confirmed past)
INSERT INTO reservations (id, user_id, room_id, start_date, end_date, total_amount_amount, total_amount_currency, status, created_at)
VALUES ('d0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000002',
        CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE - INTERVAL '7 days', 750.00, 'USD', 'CONFIRMED', NOW() - INTERVAL '15 days')
ON CONFLICT (id) DO NOTHING;

-- Reservation 2: Bob at Tokyo Skyline (upcoming confirmed)
INSERT INTO reservations (id, user_id, room_id, start_date, end_date, total_amount_amount, total_amount_currency, status, created_at)
VALUES ('d0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000007',
        CURRENT_DATE + INTERVAL '5 days', CURRENT_DATE + INTERVAL '10 days', 1000.00, 'USD', 'CONFIRMED', NOW() - INTERVAL '3 days')
ON CONFLICT (id) DO NOTHING;

-- Reservation 3: Carol at Manhattan Central (pending payment)
INSERT INTO reservations (id, user_id, room_id, start_date, end_date, total_amount_amount, total_amount_currency, status, created_at)
VALUES ('d0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000011',
        CURRENT_DATE + INTERVAL '15 days', CURRENT_DATE + INTERVAL '18 days', 900.00, 'USD', 'DRAFT', NOW() - INTERVAL '1 day')
ON CONFLICT (id) DO NOTHING;

-- Reservation 4: David at Bali Serenity (confirmed upcoming)
INSERT INTO reservations (id, user_id, room_id, start_date, end_date, total_amount_amount, total_amount_currency, status, created_at)
VALUES ('d0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000007', 'c0000000-0000-0000-0000-000000000016',
        CURRENT_DATE + INTERVAL '20 days', CURRENT_DATE + INTERVAL '27 days', 3150.00, 'USD', 'CONFIRMED', NOW() - INTERVAL '5 days')
ON CONFLICT (id) DO NOTHING;

-- Reservation 5: Emma at Grand Palace (cancelled)
INSERT INTO reservations (id, user_id, room_id, start_date, end_date, total_amount_amount, total_amount_currency, status, created_at)
VALUES ('d0000000-0000-0000-0000-000000000005', 'a0000000-0000-0000-0000-000000000008', 'c0000000-0000-0000-0000-000000000005',
        CURRENT_DATE + INTERVAL '3 days', CURRENT_DATE + INTERVAL '6 days', 825.00, 'USD', 'CANCELLED', NOW() - INTERVAL '7 days')
ON CONFLICT (id) DO NOTHING;

-- Reservation 6: Alice at Manhattan Central (upcoming confirmed)
INSERT INTO reservations (id, user_id, room_id, start_date, end_date, total_amount_amount, total_amount_currency, status, created_at)
VALUES ('d0000000-0000-0000-0000-000000000006', 'a0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000012',
        CURRENT_DATE + INTERVAL '30 days', CURRENT_DATE + INTERVAL '33 days', 1650.00, 'USD', 'CONFIRMED', NOW() - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

-- ===================== PAYMENTS =====================

-- Payment for Reservation 1
INSERT INTO payments (id, reservation_id, amount_amount, amount_currency, status, payment_method, transaction_id, created_at)
VALUES ('e0000000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', 750.00, 'USD', 'COMPLETED', 'stripe', 'txn_001_simulated', NOW() - INTERVAL '15 days')
ON CONFLICT (id) DO NOTHING;

-- Payment for Reservation 2
INSERT INTO payments (id, reservation_id, amount_amount, amount_currency, status, payment_method, transaction_id, created_at)
VALUES ('e0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000002', 1000.00, 'USD', 'COMPLETED', 'stripe', 'txn_002_simulated', NOW() - INTERVAL '3 days')
ON CONFLICT (id) DO NOTHING;

-- Payment for Reservation 4
INSERT INTO payments (id, reservation_id, amount_amount, amount_currency, status, payment_method, transaction_id, created_at)
VALUES ('e0000000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000004', 3150.00, 'USD', 'COMPLETED', 'stripe', 'txn_003_simulated', NOW() - INTERVAL '5 days')
ON CONFLICT (id) DO NOTHING;

-- Payment for Reservation 6
INSERT INTO payments (id, reservation_id, amount_amount, amount_currency, status, payment_method, transaction_id, created_at)
VALUES ('e0000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000006', 1650.00, 'USD', 'COMPLETED', 'stripe', 'txn_004_simulated', NOW() - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

-- ===================== REVIEWS =====================

-- Alice reviews Grand Palace
INSERT INTO reviews (id, hotel_id, user_id, reservation_id, rating, comment, created_at)
VALUES ('f0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000004',
        'd0000000-0000-0000-0000-000000000001', 5,
        'Absolutely stunning hotel! The view of the Eiffel Tower from our room was breathtaking. The staff was incredibly attentive and the restaurant served the best croissants I have ever had.',
        NOW() - INTERVAL '5 days')
ON CONFLICT (id) DO NOTHING;

-- Bob reviews Tokyo Skyline (after previous stay)
INSERT INTO reviews (id, hotel_id, user_id, rating, comment, created_at)
VALUES ('f0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000005',
        4, 'Great location in Shinjuku with amazing city views. The Japanese bath in the room was a wonderful touch. Slightly noisy at night due to the city location.',
        NOW() - INTERVAL '20 days')
ON CONFLICT (id) DO NOTHING;

-- Carol reviews Manhattan Central
INSERT INTO reviews (id, hotel_id, user_id, rating, comment, created_at)
VALUES ('f0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000006',
        4, 'Perfect location for exploring Manhattan. Walking distance to Broadway and Central Park. Room was clean and modern. Would definitely stay again!',
        NOW() - INTERVAL '30 days')
ON CONFLICT (id) DO NOTHING;

-- David reviews Bali Serenity
INSERT INTO reviews (id, hotel_id, user_id, rating, comment, created_at)
VALUES ('f0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000004', 'a0000000-0000-0000-0000-000000000007',
        5, 'Paradise on earth! The private pool villa was incredible. Waking up to the sounds of nature and having breakfast overlooking the rice terraces was magical. The spa treatments were world-class.',
        NOW() - INTERVAL '45 days')
ON CONFLICT (id) DO NOTHING;

-- Emma reviews Grand Palace
INSERT INTO reviews (id, hotel_id, user_id, rating, comment, created_at)
VALUES ('f0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000008',
        4, 'Beautiful hotel with excellent service. The lobby is gorgeous and the rooms are very comfortable. The only minor issue was slow WiFi during peak hours.',
        NOW() - INTERVAL '60 days')
ON CONFLICT (id) DO NOTHING;
