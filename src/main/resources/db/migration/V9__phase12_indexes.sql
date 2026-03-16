-- Phase 12: Performance & Production Readiness - Database Indexes
-- Optimizes query performance for frequently accessed columns

-- Hotel search optimization
CREATE INDEX IF NOT EXISTS idx_hotels_city_country ON hotels(city, country);
CREATE INDEX IF NOT EXISTS idx_hotels_featured ON hotels(is_featured, city);

-- Room queries optimization
CREATE INDEX IF NOT EXISTS idx_rooms_hotel_id_type ON rooms(hotel_id, type);
CREATE INDEX IF NOT EXISTS idx_rooms_base_rate_v9 ON rooms(base_rate_amount);

-- Reservation queries optimization
CREATE INDEX IF NOT EXISTS idx_reservations_user_id_status ON reservations(user_id, status);
CREATE INDEX IF NOT EXISTS idx_reservations_room_id_status ON reservations(room_id, status);
CREATE INDEX IF NOT EXISTS idx_reservations_start_date ON reservations(start_date);
CREATE INDEX IF NOT EXISTS idx_reservations_end_date ON reservations(end_date);

-- Availability range queries optimization
CREATE INDEX IF NOT EXISTS idx_availability_room_date_range ON availability(room_id, start_date, end_date);

-- Payment queries optimization
CREATE INDEX IF NOT EXISTS idx_payments_reservation_id_status ON payments(reservation_id, status);
CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payments(created_at);

-- Pricing rules queries optimization
CREATE INDEX IF NOT EXISTS idx_pricing_rules_room_date_range ON pricing_rules(room_id, start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_pricing_rules_active ON pricing_rules(start_date, end_date);

-- User queries optimization
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Audit log queries optimization (only if audit_logs table exists)
DO $$ BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'audit_logs') THEN
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_audit_logs_resource_type_timestamp ON audit_logs(resource_type, timestamp DESC)';
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_audit_logs_action_status ON audit_logs(action, status)';
  END IF;
END $$;
