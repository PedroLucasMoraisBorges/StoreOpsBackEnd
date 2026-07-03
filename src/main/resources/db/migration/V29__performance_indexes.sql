-- people: company_id não possui nenhum índice (full scan em toda query por empresa)
CREATE INDEX idx_people_company_id ON people(company_id);
CREATE INDEX idx_people_company_type ON people(company_id, type);
CREATE INDEX idx_people_company_active ON people(company_id, is_active);

-- orders: company_id sem índice — tabela cresce sem limite
CREATE INDEX idx_orders_company_id ON orders(company_id);
CREATE INDEX idx_orders_company_status ON orders(company_id, status);
CREATE INDEX idx_orders_company_created ON orders(company_id, created_at DESC);
CREATE INDEX idx_orders_customer ON orders(customer_person_id) WHERE customer_person_id IS NOT NULL;

-- order_items: FK sem índice — toda busca de itens de pedido causa full scan
CREATE INDEX idx_order_items_order_id ON order_items(order_id);

-- accounts
CREATE INDEX idx_accounts_company_id ON accounts(company_id);
CREATE INDEX idx_accounts_person_id ON accounts(person_id);
CREATE INDEX idx_accounts_company_status ON accounts(company_id, status);

-- account_transactions: FKs sem índice
CREATE INDEX idx_account_transactions_account_id ON account_transactions(account_id);
CREATE INDEX idx_account_transactions_user_id ON account_transactions(user_id);
CREATE INDEX idx_account_transactions_account_created ON account_transactions(account_id, created_at DESC);

-- product_variants, extras, groups, options: FKs sem índice
CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);
CREATE INDEX idx_product_extras_product_id ON product_extras(product_id);
CREATE INDEX idx_product_component_groups_product_id ON product_component_groups(product_id);
CREATE INDEX idx_product_component_options_group_id ON product_component_options(group_id);

-- stock_items: acessos por produto e variante frequentes
CREATE INDEX idx_stock_items_product_id ON stock_items(product_id);
CREATE INDEX idx_stock_items_variant_id ON stock_items(variant_id) WHERE variant_id IS NOT NULL;
CREATE INDEX idx_stock_items_component_option_id ON stock_items(component_option_id) WHERE component_option_id IS NOT NULL;

-- stock_movements: user_id e compound para paginação temporal
CREATE INDEX idx_stock_movements_user_id ON stock_movements(user_id);
CREATE INDEX idx_stock_movements_company_created ON stock_movements(company_id, created_at DESC);

-- cash_registers: sem nenhum índice
CREATE INDEX idx_cash_registers_company_id ON cash_registers(company_id);
CREATE INDEX idx_cash_registers_user_id ON cash_registers(user_id);

-- Garante unicidade no nível DB: apenas 1 caixa OPEN por empresa
-- Substitui a verificação em código, tornando-a atômica
CREATE UNIQUE INDEX uq_cash_register_open_per_company
    ON cash_registers(company_id)
    WHERE status = 'OPEN';

-- table_session_items: product_id/variant_id para lookups reversos de estoque
CREATE INDEX idx_table_session_items_product_id ON table_session_items(product_id) WHERE product_id IS NOT NULL;
CREATE INDEX idx_table_session_items_variant_id ON table_session_items(variant_id) WHERE variant_id IS NOT NULL;

-- table_session_payments: FK sem índice
CREATE INDEX idx_tsp_payment_method ON table_session_payments(payment_method_id);
