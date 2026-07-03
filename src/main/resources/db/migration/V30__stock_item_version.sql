-- Coluna de versão para controle de concorrência otimista em stock_items.
-- Hibernate incrementa este valor a cada UPDATE; se dois processos leram o mesmo
-- valor e ambos tentam gravar, o segundo recebe OptimisticLockException.
ALTER TABLE stock_items ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
