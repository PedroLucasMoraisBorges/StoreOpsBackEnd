-- Component-level stock: link stock_items to a component option
ALTER TABLE stock_items
    ADD COLUMN component_option_id TEXT REFERENCES product_component_options(id) ON DELETE CASCADE;

-- Ensure a row can't have both variant_id and component_option_id set
ALTER TABLE stock_items
    ADD CONSTRAINT chk_stock_type_exclusive
    CHECK (variant_id IS NULL OR component_option_id IS NULL);

-- Drop old product-level unique index and recreate requiring component_option_id IS NULL as well
DROP INDEX IF EXISTS uq_stock_product_no_variant;
CREATE UNIQUE INDEX uq_stock_product_no_variant
    ON stock_items (company_id, product_id)
    WHERE variant_id IS NULL AND component_option_id IS NULL;

-- Unique index: one stock entry per component option per company
CREATE UNIQUE INDEX uq_stock_component_option
    ON stock_items (company_id, component_option_id)
    WHERE component_option_id IS NOT NULL;

-- Track which product/variant generated a table session item (for stock restoration on removal)
ALTER TABLE table_session_items
    ADD COLUMN product_id TEXT REFERENCES products(id) ON DELETE SET NULL;
ALTER TABLE table_session_items
    ADD COLUMN variant_id TEXT REFERENCES product_variants(id) ON DELETE SET NULL;
