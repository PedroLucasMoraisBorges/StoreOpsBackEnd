CREATE TABLE cash_registers (
    id VARCHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    shift VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    opened_at TIMESTAMPTZ NOT NULL,
    closed_at TIMESTAMPTZ,
    notes TEXT,
    CONSTRAINT fk_cash_register_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_cash_register_user FOREIGN KEY (user_id) REFERENCES users(id)
);
