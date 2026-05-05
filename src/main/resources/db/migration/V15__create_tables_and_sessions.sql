CREATE TABLE store_tables (
    id TEXT PRIMARY KEY,
    company_id TEXT NOT NULL,
    number INTEGER NOT NULL,
    sector TEXT,
    capacity INTEGER,
    status TEXT NOT NULL DEFAULT 'FREE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_store_tables_company
        FOREIGN KEY (company_id)
        REFERENCES companies (id),

    CONSTRAINT uq_table_number_company UNIQUE (company_id, number)
);

CREATE INDEX idx_store_tables_company ON store_tables (company_id);

CREATE TABLE table_sessions (
    id TEXT PRIMARY KEY,
    table_id TEXT NOT NULL,
    company_id TEXT NOT NULL,
    opened_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    closed_at TIMESTAMPTZ,
    status TEXT NOT NULL DEFAULT 'OPEN',
    notes TEXT,

    CONSTRAINT fk_session_table
        FOREIGN KEY (table_id)
        REFERENCES store_tables (id),

    CONSTRAINT fk_session_company
        FOREIGN KEY (company_id)
        REFERENCES companies (id)
);

CREATE INDEX idx_table_sessions_company ON table_sessions (company_id);
CREATE INDEX idx_table_sessions_table ON table_sessions (table_id);

CREATE TABLE table_session_items (
    id TEXT PRIMARY KEY,
    session_id TEXT NOT NULL,
    name TEXT NOT NULL,
    quantity NUMERIC(15,2) NOT NULL,
    unit TEXT NOT NULL DEFAULT 'un',
    unit_price NUMERIC(15,2) NOT NULL,
    added_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_session_item_session
        FOREIGN KEY (session_id)
        REFERENCES table_sessions (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_session_items_session ON table_session_items (session_id);
