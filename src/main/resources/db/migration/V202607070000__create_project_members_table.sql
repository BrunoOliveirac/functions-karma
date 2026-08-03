CREATE TABLE project_members (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    member_id UUID NOT NULL,
    project_id UUID NOT NULL,
    CONSTRAINT fk_project_members_user
        FOREIGN KEY (user_id)
        REFERENCES users(id),
    CONSTRAINT fk_project_members_member
        FOREIGN KEY (member_id)
        REFERENCES users(id),
    CONSTRAINT fk_project_members_project
        FOREIGN KEY (project_id)
        REFERENCES projects(id),
    CONSTRAINT unique_project_member UNIQUE (user_id, member_id, project_id)
);
