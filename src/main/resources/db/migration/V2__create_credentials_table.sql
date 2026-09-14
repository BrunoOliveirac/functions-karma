CREATE TABLE IF NOT EXISTS credentials (
    id UUID PRIMARY KEY,
    hash VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT unique_credentials_user_id UNIQUE (user_id),
    CONSTRAINT fk_credentials_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);
