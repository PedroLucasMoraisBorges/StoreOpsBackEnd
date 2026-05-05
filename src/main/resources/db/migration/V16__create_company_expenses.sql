CREATE TABLE company_expenses (
    id TEXT PRIMARY KEY,
    company_id TEXT NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    description TEXT NOT NULL,
    category TEXT,
    expense_date TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_company_expenses_company
        FOREIGN KEY (company_id)
        REFERENCES companies (id)
);

CREATE INDEX idx_company_expenses_company ON company_expenses (company_id);
CREATE INDEX idx_company_expenses_date ON company_expenses (company_id, expense_date);
