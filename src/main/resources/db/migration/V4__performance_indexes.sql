-- Flyway migration: Phase 5 - Add indexes for performance optimization
-- Creates indexes on foreign keys and frequently queried columns

-- User indexes (email already has unique index from table definition)
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Hotel indexes
CREATE INDEX IF NOT EXISTS idx_hotels_city ON hotels(city);
CREATE INDEX IF NOT EXISTS idx_hotels_country ON hotels(country);
CREATE INDEX IF NOT EXISTS idx_hotels_city_country ON hotels(city, country);

-- Room indexes (hotel_id already indexed from V1)
CREATE INDEX IF NOT EXISTS idx_rooms_type ON rooms(type);
CREATE INDEX IF NOT EXISTS idx_rooms_hotel_type ON rooms(hotel_id, type);

-- Reservation indexes (user_id and room_id already indexed from V1)
CREATE INDEX IF NOT EXISTS idx_reservations_status ON reservations(status);
CREATE INDEX IF NOT EXISTS idx_reservations_room_status ON reservations(room_id, status);
CREATE INDEX IF NOT EXISTS idx_reservations_created_at ON reservations(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_reservations_dates ON reservations(start_date, end_date);

-- Availability indexes (unique constraint on room_id, start_date, end_date already exists from V1)
CREATE INDEX IF NOT EXISTS idx_availability_room_dates ON availability(room_id, start_date);

-- Pricing rule indexes (room_id already indexed from V1)
CREATE INDEX IF NOT EXISTS idx_pricing_rules_dates ON pricing_rules(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_pricing_rules_room_dates ON pricing_rules(room_id, start_date, end_date);

-- Payment indexes (reservation_id already indexed from V1)
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payments(created_at DESC);

-- Refresh token indexes (already created in V3)
-- idx_refresh_tokens_user_id, idx_refresh_tokens_token, idx_refresh_tokens_expires_at
