CREATE TABLE geo_location
(
    id             	BIGSERIAL 		PRIMARY KEY,
    postal_code     VARCHAR(32) 	NULL 			DEFAULT NULL,
    city           	VARCHAR(128) 	NULL 			DEFAULT NULL,
    county          VARCHAR(128) 	NULL 			DEFAULT NULL,
    country         VARCHAR(128) 	NULL 			DEFAULT NULL
);

CREATE UNIQUE INDEX idx_geo_location_unique
	ON geo_location (postal_code, city, county, country);

CREATE INDEX idx_geo_location_city_country
	ON geo_location(city, country);

CREATE INDEX idx_geo_location_country_city
	ON geo_location(country, city);

INSERT INTO geo_location (postal_code, city, county, country)
VALUES ('unknown', 'unknown', 'unknown', 'unknown');

ALTER TABLE love_location ADD COLUMN geo_location_id BIGINT	REFERENCES geo_location (id) NULL DEFAULT NULL;
CREATE INDEX idx_love_location_geo_location_id 	ON love_location (geo_location_id);

ALTER TABLE love_location ADD COLUMN type VARCHAR(64) NOT NULL DEFAULT 'PUBLIC_SPACE';
CREATE INDEX idx_love_location_type ON love_location (type);
