CREATE TABLE partnership
(
    id            BIGSERIAL PRIMARY KEY,
    status        VARCHAR(255)                   NOT NULL,
    initiator_id  BIGINT REFERENCES smacker (id) NOT NULL,
    respondent_id BIGINT REFERENCES smacker (id) NOT NULL,
    initiate_date TIMESTAMP                      NULL,
    respond_date  TIMESTAMP                      NULL,
    end_date      TIMESTAMP                      NULL
);
CREATE UNIQUE INDEX idx_partnership_initiator_respondent_status ON partnership (initiator_id, respondent_id, status);
CREATE INDEX idx_partnership_respondent ON partnership (respondent_id);
CREATE INDEX idx_partnership_initiate_date ON partnership (initiate_date);
CREATE INDEX idx_partnership_respond_date ON partnership (respond_date);
CREATE INDEX idx_partnership_partnership_status ON partnership (status);
