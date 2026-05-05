ALTER TABLE orders ADD COLUMN payment_method_id TEXT REFERENCES payment_methods(id);
ALTER TABLE orders ADD COLUMN paid_at TIMESTAMPTZ;

CREATE INDEX idx_orders_payment_method_id ON orders(payment_method_id);
