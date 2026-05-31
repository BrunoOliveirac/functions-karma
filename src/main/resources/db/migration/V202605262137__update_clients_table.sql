ALTER TABLE clients
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

ALTER TABLE clients
SET sector_id = NULL
WHERE sector_id NOT IN (SELECT id FROM sectorId);

ALTER TABLE clients
ADD CONSTRAINT fk_client_sectorId
FOREIGN KEY (sector_id)
REFERENCES sectorId(id)
ON DELETE SET NULL;