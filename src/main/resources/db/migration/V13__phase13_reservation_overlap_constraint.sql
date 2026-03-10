-- Phase 13: DB-level safety net to prevent overlapping active reservations for the same room
-- Active reservations are those that should block availability.
ALTER TABLE reservations
    ADD CONSTRAINT reservations_no_active_overlap
    EXCLUDE USING gist (
        room_id WITH =,
        daterange(start_date, end_date, '[)') WITH &&
    )
    WHERE (status IN ('CONFIRMED', 'PENDING_PAYMENT'));
