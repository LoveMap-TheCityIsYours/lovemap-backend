ALTER TABLE lover
    ADD COLUMN link VARCHAR(255) NULL;
CREATE INDEX idx_lover_link ON lover (link);
