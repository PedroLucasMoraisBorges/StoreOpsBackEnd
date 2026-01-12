CREATE TABLE payment_methods (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

CREATE TABLE company_payment_methods (
    company_id TEXT NOT NULL,
    payment_method_id TEXT NOT NULL,

    PRIMARY KEY (company_id, payment_method_id),
    FOREIGN KEY (company_id)
        REFERENCES companies(id)
        ON DELETE CASCADE,
    FOREIGN KEY (payment_method_id)
        REFERENCES payment_methods(id)
        ON DELETE RESTRICT
);

ALTER TABLE companies
    ADD COLUMN type TEXT NOT NULL DEFAULT 'UNDEFINED',
    ADD COLUMN address TEXT,
    ADD COLUMN phone TEXT,
    ADD COLUMN team_size TEXT,
    ADD COLUMN form_of_service TEXT,
    ADD COLUMN notification_new_order BOOLEAN NOT NULL DEFAULT true,
    ADD COLUMN weekly_reports BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN notification_in_email BOOLEAN NOT NULL DEFAULT true,
    ADD COLUMN notification_for_accounts BOOLEAN NOT NULL DEFAULT false;
