-- Seed demo data for H2 demo profile

-- Two demo users: one customer and one admin. Passwords are BCrypt hashes for demo only.
INSERT INTO users (id, email, display_name, role, password_hash) VALUES
('11111111-1111-1111-1111-111111111111', 'demo@hotresvib.com', 'Demo User', 'CUSTOMER', '$2b$12$p7PL2.a0tcVdWrRevCZLE.x.Sb7Fdo1W.mi1LnVLhRadSKM4ZpntS'),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'admin@hotresvib.com', 'Admin User', 'ADMIN', '$2b$12$obeFegv8ooJ6hnkIuqrBL.SRrDHZ04kvK/jRmQWPtx5s2N6b/o2Xy');

INSERT INTO hotels (id, name, city, country) VALUES
('22222222-2222-2222-2222-222222222222', 'Demo Hotel', 'Demo City', 'Demo Country');

INSERT INTO rooms (id, hotel_id, number, type, base_rate_amount, base_rate_currency) VALUES
('33333333-3333-3333-3333-333333333333', '22222222-2222-2222-2222-222222222222', '101', 'STANDARD', 100.00, 'USD');

INSERT INTO availability (id, room_id, start_date, end_date, available) VALUES
('44444444-4444-4444-4444-444444444444', '33333333-3333-3333-3333-333333333333', '2026-01-01', '2026-12-31', 5);

INSERT INTO pricing_rules (id, room_id, start_date, end_date, price_amount, price_currency, description) VALUES
('PR1', '33333333-3333-3333-3333-333333333333', '2026-06-01', '2026-08-31', 150.00, 'USD', 'Summer peak');

-- Sample reservation for the demo user
INSERT INTO reservations (id, user_id, room_id, start_date, end_date, total_amount_amount, total_amount_currency, status, created_at) VALUES
('55555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333', '2026-07-10', '2026-07-15', 750.00, 'USD', 'CONFIRMED', '2026-01-10 09:00:00');
