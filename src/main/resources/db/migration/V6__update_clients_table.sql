ALTER TABLE clients
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

ALTER TABLE clients
ADD COLUMN IF NOT EXISTS sector_id UUID NULL;

UPDATE clients
SET sector_id = NULL
WHERE sector_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sectors s WHERE s.id = clients.sector_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_client_sector'
    ) THEN
        ALTER TABLE clients
        ADD CONSTRAINT fk_client_sector
        FOREIGN KEY (sector_id)
        REFERENCES sectors(id)
        ON DELETE SET NULL;
    END IF;
END $$;
