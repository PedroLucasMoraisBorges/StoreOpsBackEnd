ALTER TABLE companies
    ALTER COLUMN id TYPE TEXT USING id::text;

ALTER TABLE users
    ALTER COLUMN id TYPE TEXT USING id::text;

ALTER TABLE people
    ALTER COLUMN id TYPE TEXT USING id::text,
    ALTER COLUMN company_id TYPE TEXT USING company_id::text,
    ALTER COLUMN user_id TYPE TEXT USING user_id::text;

ALTER TABLE user_companies
    ALTER COLUMN user_id TYPE TEXT USING user_id::text,
    ALTER COLUMN company_id TYPE TEXT USING company_id::text;

ALTER TABLE accounts
    ALTER COLUMN id TYPE TEXT USING id::text,
    ALTER COLUMN company_id TYPE TEXT USING company_id::text,
    ALTER COLUMN person_id TYPE TEXT USING person_id::text;

ALTER TABLE account_transactions
    ALTER COLUMN id TYPE TEXT USING id::text,
    ALTER COLUMN account_id TYPE TEXT USING account_id::text,
    ALTER COLUMN user_id TYPE TEXT USING user_id::text;
