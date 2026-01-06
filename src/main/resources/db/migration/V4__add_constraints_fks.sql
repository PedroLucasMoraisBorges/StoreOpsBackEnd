ALTER TABLE people
    ADD CONSTRAINT fk_people_company
        FOREIGN KEY (company_id) REFERENCES companies (id),
    ADD CONSTRAINT fk_people_user
        FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE user_companies
    ADD CONSTRAINT fk_uc_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    ADD CONSTRAINT fk_uc_company
        FOREIGN KEY (company_id) REFERENCES companies (id);

ALTER TABLE accounts
    ADD CONSTRAINT fk_account_company
        FOREIGN KEY (company_id) REFERENCES companies (id),
    ADD CONSTRAINT fk_account_person
        FOREIGN KEY (person_id) REFERENCES people (id);

ALTER TABLE account_transactions
    ADD CONSTRAINT fk_transaction_account
        FOREIGN KEY (account_id) REFERENCES accounts (id),
    ADD CONSTRAINT fk_transaction_user
        FOREIGN KEY (user_id) REFERENCES users (id);
