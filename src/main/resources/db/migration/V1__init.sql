CREATE TABLE smacker
(
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(255) UNIQUE            NOT NULL,
    password_hash VARCHAR(255)                   NOT NULL,
    email         VARCHAR(255) UNIQUE            NOT NULL,
    partner_id    BIGINT REFERENCES smacker (id) NULL
);
CREATE INDEX idx_smacker_partner ON smacker (partner_id);

CREATE TABLE smack_location
(
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    longitude      FLOAT        NOT NULL,
    latitude       FLOAT        NOT NULL,
    average_rating FLOAT        NULL
);
CREATE INDEX idx_smack_location_longitude ON smack_location (longitude);
CREATE INDEX idx_smack_location_latitude ON smack_location (latitude);
CREATE INDEX idx_smack_location_average_rating ON smack_location (average_rating);

CREATE TABLE smack
(
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(255)                          NOT NULL,
    smack_location_id  BIGINT REFERENCES smack_location (id) NOT NULL,
    smacker_id         BIGINT REFERENCES smacker (id)        NOT NULL,
    smacker_partner_id BIGINT REFERENCES smacker (id)        NULL
);
CREATE INDEX idx_smack_smack_location_id ON smack (smack_location_id);
CREATE INDEX idx_smack_smacker_id ON smack (smacker_id);
CREATE INDEX idx_smack_smacker_partner_id ON smack (smacker_partner_id);

CREATE TABLE smack_location_review
(
    id                BIGSERIAL PRIMARY KEY,
    smack_id          BIGINT REFERENCES smack (id)          NOT NULL,
    reviewer_id       BIGINT REFERENCES smacker (id)        NOT NULL,
    smack_location_id BIGINT REFERENCES smack_location (id) NOT NULL,
    review_text       TEXT                                  NOT NULL,
    review_stars      SMALLSERIAL                           NOT NULL
);
CREATE INDEX idx_smack_location_review_smack_id ON smack_location_review (smack_id);
CREATE INDEX idx_smack_location_review_reviewer_id ON smack_location_review (reviewer_id);
CREATE INDEX idx_smack_location_review_smack_location_id ON smack_location_review (smack_location_id);
CREATE UNIQUE INDEX idx_smack_location_review_reviewer_unique ON smack_location_review (reviewer_id, smack_location_id);
