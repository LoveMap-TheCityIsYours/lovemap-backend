ALTER TABLE password RENAME TO lover_authentication;

ALTER TABLE lover_authentication ADD COLUMN password_set BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE lover_authentication ADD COLUMN facebook_connected BOOLEAN NOT NULL DEFAULT FALSE;
