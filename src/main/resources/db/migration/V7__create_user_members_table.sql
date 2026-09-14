CREATE TABLE IF NOT EXISTS user_members (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    member_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_user_members_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_members_member
        FOREIGN KEY (member_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT unique_user_member UNIQUE (user_id, member_id)
);
