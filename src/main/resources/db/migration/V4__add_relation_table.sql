DROP INDEX idx_smacker_partner;
ALTER TABLE smacker
    DROP COLUMN partner_id;

ALTER TABLE smacker
    ADD COLUMN public_profile BOOLEAN DEFAULT FALSE NOT NULL;

CREATE TABLE relation
(
    id        BIGSERIAL PRIMARY KEY,
    status    VARCHAR(255)                   NOT NULL,
    source_id BIGINT REFERENCES smacker (id) NOT NULL,
    target_id BIGINT REFERENCES smacker (id) NOT NULL
);
CREATE UNIQUE INDEX idx_relation_source_target_status ON relation (source_id, target_id, status);
CREATE INDEX idx_relation_target ON relation (target_id);
CREATE INDEX idx_relation_relation_status ON relation (status);
