CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE hotels (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    city TEXT NOT NULL,
    country TEXT NOT NULL
);

CREATE TABLE rooms (
    id UUID PRIMARY KEY,
    hotel_id UUID NOT NULL REFERENCES hotels(id) ON DELETE RESTRICT,
    number TEXT NOT NULL,
    type TEXT NOT NULL,
    base_rate NUMERIC(10, 2) NOT NULL
);

CREATE INDEX idx_rooms_hotel_id ON rooms(hotel_id);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    display_name TEXT NOT NULL,
    role TEXT NOT NULL
);

CREATE TABLE reservations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    room_id UUID NOT NULL REFERENCES rooms(id) ON DELETE RESTRICT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_amount NUMERIC(10, 2) NOT NULL,
    status TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_reservations_user_id ON reservations(user_id);
CREATE INDEX idx_reservations_room_id ON reservations(room_id);

CREATE TABLE availability (
    room_id UUID NOT NULL REFERENCES rooms(id) ON DELETE RESTRICT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    available INT NOT NULL,
    PRIMARY KEY (room_id, start_date, end_date)
);

ALTER TABLE availability
    ADD CONSTRAINT availability_no_overlap
    EXCLUDE USING gist (room_id WITH =, daterange(start_date, end_date, '[]') WITH &&);

CREATE INDEX idx_availability_room_dates ON availability(room_id, start_date, end_date);

CREATE TABLE pricing_rules (
    id TEXT PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES rooms(id) ON DELETE RESTRICT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    description TEXT
);

CREATE INDEX idx_pricing_rules_room_id ON pricing_rules(room_id);

CREATE TABLE payments (
    id TEXT PRIMARY KEY,
    reservation_id UUID NOT NULL REFERENCES reservations(id) ON DELETE RESTRICT,
    amount NUMERIC(10, 2) NOT NULL,
    status TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_payments_reservation_id ON payments(reservation_id);
