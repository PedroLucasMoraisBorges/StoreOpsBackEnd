ALTER TABLE table_sessions ADD COLUMN payment_method_id TEXT REFERENCES payment_methods(id);
ALTER TABLE table_sessions ADD COLUMN paid_at TIMESTAMPTZ;

CREATE INDEX idx_table_sessions_company_closed ON table_sessions(company_id, status, closed_at);
