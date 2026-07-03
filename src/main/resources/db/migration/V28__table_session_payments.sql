CREATE TABLE table_session_payments (
  id TEXT PRIMARY KEY DEFAULT gen_random_uuid(),
  session_id TEXT NOT NULL REFERENCES table_sessions(id),
  payment_method_id TEXT NOT NULL REFERENCES payment_methods(id),
  amount NUMERIC(10,2) NOT NULL,
  paid_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tsp_session ON table_session_payments(session_id);
