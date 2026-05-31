CREATE INDEX IF NOT EXISTS idx_clients_user_email_active
ON clients (user_id, email)
WHERE deleted_at IS NULL;