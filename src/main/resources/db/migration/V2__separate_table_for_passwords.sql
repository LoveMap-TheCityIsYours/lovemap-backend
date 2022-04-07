ALTER TABLE smacker
    DROP COLUMN password_hash;

CREATE TABLE password
(
    id            BIGSERIAL PRIMARY KEY,
    smacker_id    BIGINT UNIQUE NOT NULL REFERENCES smacker (id),
    password_hash VARCHAR(255)  NOT NULL
);
CREATE INDEX idx_password_smacker_id ON password (smacker_id);
