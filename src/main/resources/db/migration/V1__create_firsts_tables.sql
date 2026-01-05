CREATE TABLE companies (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    login TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL
);

CREATE TABLE people (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    user_id UUID,

    CONSTRAINT fk_people_company
        FOREIGN KEY (company_id)
        REFERENCES companies (id),

    CONSTRAINT fk_people_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);

CREATE TABLE user_companies (
    user_id UUID NOT NULL,
    company_id UUID NOT NULL,
    role TEXT NOT NULL,

    PRIMARY KEY (user_id, company_id),

    CONSTRAINT fk_uc_user
        FOREIGN KEY (user_id)
        REFERENCES users (id),

    CONSTRAINT fk_uc_company
        FOREIGN KEY (company_id)
        REFERENCES companies (id)
);

CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    company_id UUID NOT NULL,
    person_id UUID NOT NULL,
    status TEXT NOT NULL,
    opened_at TIMESTAMP NOT NULL DEFAULT now(),
    closed_at TIMESTAMP,

    CONSTRAINT fk_account_company
        FOREIGN KEY (company_id)
        REFERENCES companies (id),

    CONSTRAINT fk_account_person
        FOREIGN KEY (person_id)
        REFERENCES people (id)
);

CREATE TABLE account_transactions (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    origin TEXT NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    user_id UUID NOT NULL,

    CONSTRAINT fk_transaction_account
        FOREIGN KEY (account_id)
        REFERENCES accounts (id),

    CONSTRAINT fk_transaction_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);
