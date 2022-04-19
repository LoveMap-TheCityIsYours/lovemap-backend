DROP INDEX idx_smack_location_longitude;

CREATE INDEX idx_smack_location_longitude_latitude_rating
ON smack_location (longitude, latitude, average_rating);

ALTER TABLE smack_location
	ADD COLUMN number_of_ratings INTEGER NOT NULL DEFAULT 0;
