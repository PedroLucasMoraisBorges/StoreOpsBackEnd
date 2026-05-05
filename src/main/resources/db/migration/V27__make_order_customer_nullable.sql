ALTER TABLE orders ALTER COLUMN customer_person_id DROP NOT NULL;
ALTER TABLE orders ADD COLUMN customer_name TEXT;
