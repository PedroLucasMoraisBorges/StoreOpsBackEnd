ALTER TABLE payment_methods
ADD COLUMN code TEXT NOT NULL UNIQUE;

ALTER TABLE payment_methods
DROP COLUMN method_name;

INSERT INTO payment_methods (id, code, name) VALUES
('1', 'CASH', 'Dinheiro'),
('2', 'CREDIT_CARD', 'Cartão de Crédito'),
('3', 'DEBIT_CARD', 'Cartão de Débito'),
('4', 'PIX', 'PIX');