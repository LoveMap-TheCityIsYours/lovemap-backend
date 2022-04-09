DROP INDEX idx_smacker_partner;
ALTER TABLE smacker DROP COLUMN partner_id;

CREATE TABLE partnership
(
    id                 BIGSERIAL PRIMARY KEY,
    partnership_status VARCHAR(255)                   NOT NULL,
    requestor          BIGINT REFERENCES smacker (id) NOT NULL,
    requestee          BIGINT REFERENCES smacker (id) NOT NULL,
    start_date         TIMESTAMP                      NULL,
    end_date           TIMESTAMP                      NULL
);
CREATE INDEX idx_partnership_requestor_requestee ON partnership (requestor, requestee);
CREATE INDEX idx_partnership_requestee ON partnership (requestee);
CREATE INDEX idx_partnership_start_date_end_date ON partnership (start_date, end_date);
