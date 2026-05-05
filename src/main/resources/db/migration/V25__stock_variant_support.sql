-- Drop the unique constraint on product_id to allow multiple stock items per product (one per variant)
ALTER TABLE stock_items DROP CONSTRAINT IF EXISTS stock_items_product_id_key;

-- Add variant_id column (nullable — null means product-level stock, no variant)
ALTER TABLE stock_items ADD COLUMN variant_id TEXT REFERENCES product_variants(id) ON DELETE CASCADE;

-- Unique index: one stock entry per (company, product) when no variant
CREATE UNIQUE INDEX uq_stock_product_no_variant
    ON stock_items (company_id, product_id)
    WHERE variant_id IS NULL;

-- Unique index: one stock entry per (company, product, variant)
CREATE UNIQUE INDEX uq_stock_product_variant
    ON stock_items (company_id, product_id, variant_id)
    WHERE variant_id IS NOT NULL;
