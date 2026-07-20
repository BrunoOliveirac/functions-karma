CREATE TABLE user_members (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    member_id UUID NOT NULL,
    CONSTRAINT fk_user_members_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),
    CONSTRAINT fk_user_members_member
        FOREIGN KEY (member_id)
        REFERENCES users(id),
    CONSTRAINT unique_user_member UNIQUE (user_id, member_id)
);
