CREATE TABLE product_variants (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    product_id TEXT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    price_delta NUMERIC(15,2) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE product_extras (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    product_id TEXT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    price NUMERIC(15,2) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE product_component_groups (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    product_id TEXT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    max_selections INT NOT NULL DEFAULT 1,
    required BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE product_component_options (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::TEXT,
    group_id TEXT NOT NULL REFERENCES product_component_groups(id) ON DELETE CASCADE,
    name TEXT NOT NULL
);
