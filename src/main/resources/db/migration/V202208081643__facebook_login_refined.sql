ALTER TABLE lover_authentication ALTER COLUMN password_hash DROP NOT NULL;
ALTER TABLE lover_authentication DROP COLUMN facebook_connected;
ALTER TABLE lover_authentication ADD COLUMN facebook_id VARCHAR(32) NULL;
CREATE UNIQUE INDEX idx_lover_authentication_facebook_id ON lover_authentication (facebook_id);
