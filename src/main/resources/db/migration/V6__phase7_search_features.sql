-- Phase 7: Add search and discovery features

-- Add isFeatured column to hotels table
ALTER TABLE hotels ADD COLUMN is_featured BOOLEAN NOT NULL DEFAULT false;

-- Create indexes for search performance
CREATE INDEX idx_hotels_city ON hotels(city);
CREATE INDEX idx_hotels_country ON hotels(country);
CREATE INDEX idx_hotels_is_featured ON hotels(is_featured);
CREATE INDEX idx_rooms_type ON rooms(type);
CREATE INDEX idx_rooms_base_rate ON rooms(base_rate_amount);
CREATE INDEX idx_rooms_hotel_id ON rooms(hotel_id);

-- Mark some demo hotels as featured (if demo data exists)
UPDATE hotels SET is_featured = true WHERE id IN (
    SELECT id FROM hotels ORDER BY id LIMIT 3
);
