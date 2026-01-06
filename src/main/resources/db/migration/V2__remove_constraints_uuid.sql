ALTER TABLE people DROP CONSTRAINT fk_people_company;
ALTER TABLE people DROP CONSTRAINT fk_people_user;

ALTER TABLE user_companies DROP CONSTRAINT fk_uc_user;
ALTER TABLE user_companies DROP CONSTRAINT fk_uc_company;

ALTER TABLE accounts DROP CONSTRAINT fk_account_company;
ALTER TABLE accounts DROP CONSTRAINT fk_account_person;

ALTER TABLE account_transactions DROP CONSTRAINT fk_transaction_account;
ALTER TABLE account_transactions DROP CONSTRAINT fk_transaction_user;
