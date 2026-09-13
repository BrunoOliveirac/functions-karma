CREATE TABLE IF NOT EXISTS clients (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    notes VARCHAR(500),
    user_id UUID NOT NULL,
    budget INTEGER NOT NULL,
    favorite BOOLEAN NOT NULL DEFAULT FALSE,
    sector_id UUID,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_clients_user_email_active
ON clients (user_id, email)
WHERE deleted_at IS NULL;
