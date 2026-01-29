-- Phase 7 (H2): Add search and discovery features (H2-safe)

-- Ensure is_featured exists (some earlier demo migration may have added it)
ALTER TABLE hotels ADD COLUMN IF NOT EXISTS is_featured BOOLEAN DEFAULT FALSE;

-- Create indexes for search performance (H2 supports IF NOT EXISTS)
CREATE INDEX IF NOT EXISTS idx_hotels_city_demo ON hotels(city);
CREATE INDEX IF NOT EXISTS idx_hotels_country_demo ON hotels(country);
CREATE INDEX IF NOT EXISTS idx_hotels_is_featured_demo ON hotels(is_featured);
CREATE INDEX IF NOT EXISTS idx_rooms_type_demo ON rooms(type);
CREATE INDEX IF NOT EXISTS idx_rooms_base_rate_demo ON rooms(base_rate_amount);
CREATE INDEX IF NOT EXISTS idx_rooms_hotel_id_demo ON rooms(hotel_id);

-- Mark some demo hotels as featured if data exists
UPDATE hotels SET is_featured = TRUE WHERE id IN (
    SELECT id FROM hotels ORDER BY id LIMIT 3
);
