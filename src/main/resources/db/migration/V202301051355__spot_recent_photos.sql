ALTER TABLE love_location ADD COLUMN last_photo_added_at TIMESTAMP NULL;

CREATE INDEX love_location_last_photo_added_at ON love_location (last_photo_added_at);

UPDATE love_location
SET last_photo_added_at = subquery.uploaded_at
FROM (
		SELECT love_location_photo.uploaded_at AS uploaded_at, love_location_photo.love_location_id AS love_location_id
		FROM love_location_photo
		JOIN love_location ON love_location.id = love_location_photo.love_location_id
		ORDER BY love_location_photo.uploaded_at DESC
) AS subquery
WHERE subquery.love_location_id = love_location.id;


