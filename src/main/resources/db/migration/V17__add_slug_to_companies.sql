ALTER TABLE companies ADD COLUMN slug TEXT UNIQUE;

CREATE INDEX idx_companies_slug ON companies (slug);
