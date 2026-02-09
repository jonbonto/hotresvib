-- Phase 12: Performance & Production Readiness - Database Indexes (H2 Demo)
-- Optimizes query performance for frequently accessed columns

-- Hotel search optimization
CREATE INDEX idx_hotels_city_country ON hotels(city, country);
CREATE INDEX idx_hotels_featured ON hotels(is_featured, city);

-- Room queries optimization
CREATE INDEX idx_rooms_hotel_id_type ON rooms(hotel_id, type);
CREATE INDEX idx_rooms_base_rate ON rooms(base_rate);

-- Reservation queries optimization
CREATE INDEX idx_reservations_user_id_status ON reservations(user_id, status);
CREATE INDEX idx_reservations_room_id_status ON reservations(room_id, status);
CREATE INDEX idx_reservations_check_in_date ON reservations(check_in_date);
CREATE INDEX idx_reservations_check_out_date ON reservations(check_out_date);

-- Availability range queries optimization
CREATE INDEX idx_availability_room_date_range ON availability(room_id, date_range_start, date_range_end);

-- Payment queries optimization
CREATE INDEX idx_payments_reservation_id_status ON payments(reservation_id, status);
CREATE INDEX idx_payments_created_at ON payments(created_at);

-- Pricing rules queries optimization
CREATE INDEX idx_pricing_rules_room_date_range ON pricing_rules(room_id, date_range_start, date_range_end);
CREATE INDEX idx_pricing_rules_active ON pricing_rules(date_range_start, date_range_end);

-- User queries optimization
CREATE INDEX idx_users_email_active ON users(email, active);
CREATE INDEX idx_users_created_at ON users(created_at);

-- Audit log queries optimization
CREATE INDEX idx_audit_logs_resource_type_timestamp ON audit_logs(resource_type, timestamp DESC);
CREATE INDEX idx_audit_logs_action_status ON audit_logs(action, status);
