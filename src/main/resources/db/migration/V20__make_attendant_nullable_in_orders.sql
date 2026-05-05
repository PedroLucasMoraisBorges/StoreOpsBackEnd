-- Allow online orders (no attendant) by making attendant_user_id nullable.
-- The composite FK must be dropped first because it includes the nullable column.
ALTER TABLE orders DROP CONSTRAINT fk_orders_attendant;
ALTER TABLE orders ALTER COLUMN attendant_user_id DROP NOT NULL;
