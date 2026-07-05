CREATE TABLE push_subscriptions (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_id TEXT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    endpoint TEXT NOT NULL,
    p256dh TEXT NOT NULL,
    auth TEXT NOT NULL,
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    last_success_at TIMESTAMPTZ,
    CONSTRAINT uq_push_endpoint_user_company UNIQUE (endpoint, user_id, company_id)
);

CREATE INDEX idx_push_subscriptions_company ON push_subscriptions(company_id);
CREATE INDEX idx_push_subscriptions_user ON push_subscriptions(user_id);

CREATE TABLE user_notification_preferences (
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    company_id TEXT NOT NULL REFERENCES companies(id) ON DELETE CASCADE,
    new_order BOOLEAN NOT NULL DEFAULT TRUE,
    accounts BOOLEAN NOT NULL DEFAULT TRUE,
    cash_register BOOLEAN NOT NULL DEFAULT TRUE,
    low_stock BOOLEAN NOT NULL DEFAULT TRUE,
    weekly_reports BOOLEAN NOT NULL DEFAULT FALSE,
    email BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, company_id)
);
