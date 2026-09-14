CREATE TABLE IF NOT EXISTS project_members (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    member_id UUID NOT NULL,
    project_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_project_members_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_project_members_member
        FOREIGN KEY (member_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_project_members_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id)
        ON DELETE CASCADE,
    CONSTRAINT unique_project_member UNIQUE (user_id, member_id, project_id)
);
