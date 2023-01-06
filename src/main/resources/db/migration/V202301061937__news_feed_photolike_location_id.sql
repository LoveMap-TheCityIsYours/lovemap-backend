--ALTER TABLE photo_like ADD COLUMN love_location_id BIGINT NOT NULL DEFAULT 1
--CONSTRAINT fk_photo_like_love_location_id REFERENCES love_location(id);

ALTER TABLE photo_like ADD COLUMN love_location_id BIGINT NOT NULL DEFAULT 1023
CONSTRAINT fk_photo_like_love_location_id REFERENCES love_location(id);

UPDATE photo_like
SET love_location_id = subquery.love_location_id
FROM (
		SELECT love_location_photo.love_location_id AS love_location_id, love_location_photo.id AS love_location_photo_id
		FROM love_location_photo
		JOIN photo_like ON photo_like.photo_id = love_location_photo.id
) AS subquery
WHERE subquery.love_location_photo_id = photo_like.photo_id;
