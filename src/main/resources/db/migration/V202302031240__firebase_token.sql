ALTER TABLE lover ADD COLUMN firebase_token TEXT DEFAULT NULL;
ALTER TABLE lover ADD COLUMN has_firebase_token BOOLEAN DEFAULT FALSE;

CREATE INDEX idx_lover_as_firebase_token ON lover (has_firebase_token);
