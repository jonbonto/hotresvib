-- Legacy fix kept for history but no-op for current demo schema
/*
 Previously this migration converted embedded-money column naming to a flat
 `amount`/`currency` pair. Current domain mappings use prefixed column names
 (e.g. `total_amount_amount`, `base_rate_amount`, `amount_amount`, etc.) to
 avoid collisions. To keep the demo migrations stable we intentionally keep
 this file as a documented no-op.
*/
