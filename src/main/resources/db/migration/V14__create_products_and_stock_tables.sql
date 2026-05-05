CREATE TABLE products (
    id TEXT PRIMARY KEY,
    company_id TEXT NOT NULL,
    name TEXT NOT NULL,
    category TEXT,
    unit TEXT NOT NULL DEFAULT 'un',
    cost_price NUMERIC(15,2) NOT NULL DEFAULT 0,
    sell_price NUMERIC(15,2) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_products_company
        FOREIGN KEY (company_id)
        REFERENCES companies (id)
);

CREATE INDEX idx_products_company_id ON products (company_id);
CREATE INDEX idx_products_active ON products (company_id, active);

CREATE TABLE stock_items (
    id TEXT PRIMARY KEY,
    company_id TEXT NOT NULL,
    product_id TEXT NOT NULL UNIQUE,
    quantity NUMERIC(15,3) NOT NULL DEFAULT 0,
    min_quantity NUMERIC(15,3) NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_stock_company
        FOREIGN KEY (company_id)
        REFERENCES companies (id),

    CONSTRAINT fk_stock_product
        FOREIGN KEY (product_id)
        REFERENCES products (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_stock_items_company ON stock_items (company_id);

CREATE TABLE stock_movements (
    id TEXT PRIMARY KEY,
    stock_item_id TEXT NOT NULL,
    company_id TEXT NOT NULL,
    type TEXT NOT NULL,
    quantity NUMERIC(15,3) NOT NULL,
    notes TEXT,
    user_id TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_movement_stock_item
        FOREIGN KEY (stock_item_id)
        REFERENCES stock_items (id),

    CONSTRAINT fk_movement_company
        FOREIGN KEY (company_id)
        REFERENCES companies (id),

    CONSTRAINT fk_movement_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);

CREATE INDEX idx_stock_movements_company ON stock_movements (company_id);
CREATE INDEX idx_stock_movements_stock_item ON stock_movements (stock_item_id);
