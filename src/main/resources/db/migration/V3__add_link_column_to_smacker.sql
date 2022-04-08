ALTER TABLE smacker
    ADD COLUMN link VARCHAR(255) NULL;
CREATE INDEX idx_smacker_link ON smacker (link);
