-- Activate the unaccent extension at database
CREATE EXTENSION IF NOT EXISTS unaccent;

-- Create Trident Index
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Create a function to simplify the text (remove accents and convert to lowercase)
CREATE OR REPLACE FUNCTION slugify(text_input TEXT)
RETURNS TEXT AS $$
BEGIN
    RETURN LOWER(PUBLIC.UNACCENT(text_input));
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Create users name index for accent-insensitive search
CREATE INDEX idx_users_name_slugify
ON users USING gin (slugify(name) gin_trgm_ops);