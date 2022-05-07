DROP INDEX idx_love_location_longitude;

CREATE INDEX idx_love_location_longitude_latitude_rating
ON love_location (longitude, latitude, average_rating);

ALTER TABLE love_location
	ADD COLUMN number_of_ratings INTEGER NOT NULL DEFAULT 0;
