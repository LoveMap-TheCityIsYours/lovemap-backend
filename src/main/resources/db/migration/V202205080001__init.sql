CREATE TABLE lover
(
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(255) UNIQUE            NOT NULL,
    password_hash VARCHAR(255)                   NOT NULL,
    email         VARCHAR(255) UNIQUE            NOT NULL,
    partner_id    BIGINT REFERENCES lover (id) NULL
);
CREATE INDEX idx_lover_partner ON lover (partner_id);

CREATE TABLE love_location
(
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    longitude      FLOAT        NOT NULL,
    latitude       FLOAT        NOT NULL,
    average_rating FLOAT        NULL
);
CREATE INDEX idx_love_location_longitude ON love_location (longitude);
CREATE INDEX idx_love_location_latitude ON love_location (latitude);
CREATE INDEX idx_love_location_average_rating ON love_location (average_rating);

CREATE TABLE love
(
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(255)                          NOT NULL,
    love_location_id  BIGINT REFERENCES love_location (id) NOT NULL,
    lover_id         BIGINT REFERENCES lover (id)        NOT NULL,
    lover_partner_id BIGINT REFERENCES lover (id)        NULL
);
CREATE INDEX idx_love_love_location_id ON love (love_location_id);
CREATE INDEX idx_love_lover_id ON love (lover_id);
CREATE INDEX idx_love_lover_partner_id ON love (lover_partner_id);

CREATE TABLE love_location_review
(
    id                BIGSERIAL PRIMARY KEY,
    love_id          BIGINT REFERENCES love (id)          NOT NULL,
    reviewer_id       BIGINT REFERENCES lover (id)        NOT NULL,
    love_location_id BIGINT REFERENCES love_location (id) NOT NULL,
    review_text       TEXT                                  NOT NULL,
    review_stars      SMALLSERIAL                           NOT NULL
);
CREATE INDEX idx_love_location_review_love_id ON love_location_review (love_id);
CREATE INDEX idx_love_location_review_reviewer_id ON love_location_review (reviewer_id);
CREATE INDEX idx_love_location_review_love_location_id ON love_location_review (love_location_id);
CREATE UNIQUE INDEX idx_love_location_review_reviewer_unique ON love_location_review (reviewer_id, love_location_id);
