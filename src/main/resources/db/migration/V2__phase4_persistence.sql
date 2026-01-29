-- Flyway migration: create Phase 4 persistence schema
-- Creates tables for users, hotels, rooms, reservations, payments, availability, pricing rules

CREATE TABLE users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  display_name VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE hotels (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  city VARCHAR(255) NOT NULL,
  country VARCHAR(255) NOT NULL
);

CREATE TABLE rooms (
  id UUID PRIMARY KEY,
  hotel_id UUID NOT NULL,
  number VARCHAR(50) NOT NULL,
  type VARCHAR(50) NOT NULL,
  base_rate_amount NUMERIC(19,4) NOT NULL,
  base_rate_currency VARCHAR(3) NOT NULL,
  CONSTRAINT fk_rooms_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id)
);

CREATE TABLE reservations (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  room_id UUID NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  total_amount_amount NUMERIC(19,4) NOT NULL,
  total_amount_currency VARCHAR(3) NOT NULL,
  status VARCHAR(50) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_reservations_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_reservations_room FOREIGN KEY (room_id) REFERENCES rooms(id)
);

CREATE TABLE payments (
  id UUID PRIMARY KEY,
  reservation_id UUID NOT NULL,
  amount_amount NUMERIC(19,4) NOT NULL,
  amount_currency VARCHAR(3) NOT NULL,
  status VARCHAR(50) NOT NULL,
  payment_method VARCHAR(100) NOT NULL,
  transaction_id VARCHAR(255),
  created_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_payments_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);

CREATE TABLE availability (
  id UUID PRIMARY KEY,
  room_id UUID NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  available INT NOT NULL,
  CONSTRAINT fk_availability_room FOREIGN KEY (room_id) REFERENCES rooms(id)
);

CREATE TABLE pricing_rules (
  id VARCHAR(255) PRIMARY KEY,
  room_id UUID NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  price_amount NUMERIC(19,4) NOT NULL,
  price_currency VARCHAR(3) NOT NULL,
  description VARCHAR(1024),
  CONSTRAINT fk_pricing_rule_room FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- Optional: indexes for common queries
CREATE INDEX idx_rooms_hotel ON rooms(hotel_id);
CREATE INDEX idx_reservations_user ON reservations(user_id);
CREATE INDEX idx_payments_reservation ON payments(reservation_id);
