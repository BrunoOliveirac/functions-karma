CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    code VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    reference_id UUID,
    reference_label VARCHAR(255),
    read BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    user_id UUID NOT NULL,
    actor_id UUID,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_notifications_actor
        FOREIGN KEY (actor_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

-- Supports findAllByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc
CREATE INDEX idx_notifications_user_created_at
ON notifications (user_id, created_at DESC)
WHERE deleted_at IS NULL;

CREATE INDEX idx_notifications_actor_id
ON notifications (actor_id);
