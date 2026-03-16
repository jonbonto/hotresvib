-- V14: Reviews system and hotel/room enhancements

-- Add description, address, contact fields to hotels
ALTER TABLE hotels ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE hotels ADD COLUMN IF NOT EXISTS address VARCHAR(500);
ALTER TABLE hotels ADD COLUMN IF NOT EXISTS phone VARCHAR(50);
ALTER TABLE hotels ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE hotels ADD COLUMN IF NOT EXISTS star_rating INTEGER DEFAULT 0;
ALTER TABLE hotels ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

-- Add description, capacity, amenities to rooms
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS capacity INTEGER DEFAULT 2;
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS amenities TEXT;
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

-- Reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id UUID PRIMARY KEY,
    hotel_id UUID NOT NULL,
    user_id UUID NOT NULL,
    reservation_id UUID,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_reviews_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reviews_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT uq_reviews_user_hotel UNIQUE (user_id, hotel_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_hotel ON reviews(hotel_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user ON reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_reviews_rating ON reviews(rating);
