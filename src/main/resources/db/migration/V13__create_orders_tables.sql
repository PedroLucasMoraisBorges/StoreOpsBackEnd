CREATE TABLE orders (
    id TEXT PRIMARY KEY,
    company_id TEXT NOT NULL,
    customer_person_id TEXT NOT NULL,
    attendant_user_id TEXT NOT NULL,
    type TEXT NOT NULL,
    scheduled_at TIMESTAMPTZ NOT NULL,
    delivery_address TEXT,
    notes TEXT,
    status TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,

    CONSTRAINT fk_orders_company
        FOREIGN KEY (company_id)
        REFERENCES companies (id),

    CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_person_id)
        REFERENCES people (id),

    CONSTRAINT fk_orders_attendant
        FOREIGN KEY (attendant_user_id, company_id)
        REFERENCES user_companies (user_id, company_id)
);

CREATE TABLE order_items (
    id TEXT PRIMARY KEY,
    order_id TEXT NOT NULL,
    name TEXT NOT NULL,
    quantity NUMERIC(15,2) NOT NULL,
    unit TEXT NOT NULL,
    unit_price NUMERIC(15,2) NOT NULL,

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders (id)
        ON DELETE CASCADE
);
