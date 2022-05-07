ALTER TABLE lover
    DROP COLUMN password_hash;

CREATE TABLE password
(
    id            BIGSERIAL PRIMARY KEY,
    lover_id    BIGINT UNIQUE NOT NULL REFERENCES lover (id),
    password_hash VARCHAR(255)  NOT NULL
);
CREATE INDEX idx_password_lover_id ON password (lover_id);
