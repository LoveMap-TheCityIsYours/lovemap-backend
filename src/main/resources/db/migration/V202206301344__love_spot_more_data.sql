ALTER TABLE love_location ADD COLUMN number_of_loves BIGINT NOT NULL DEFAULT 0;
ALTER TABLE love_location ADD COLUMN number_of_comments BIGINT NOT NULL DEFAULT 0;
ALTER TABLE love_location ADD COLUMN occurrence_on_wishlists BIGINT NOT NULL DEFAULT 0;
ALTER TABLE love_location ADD COLUMN popularity BIGINT NOT NULL DEFAULT 0;

ALTER TABLE love_location ADD COLUMN last_comment_at TIMESTAMP NULL DEFAULT NULL;
ALTER TABLE love_location ADD COLUMN last_love_at TIMESTAMP NULL DEFAULT NULL;
ALTER TABLE love_location ADD COLUMN last_active_at TIMESTAMP NULL DEFAULT NULL;

CREATE INDEX idx_love_location_last_active_at ON love_location(last_active_at);
CREATE INDEX idx_love_location_popularity ON love_location(popularity);

UPDATE love_location
SET number_of_loves = (
	SELECT COUNT(love.id) FROM love
	WHERE love_location.id = love.love_location_id
);

UPDATE love_location
SET popularity = 2 * number_of_loves + number_of_comments + occurrence_on_wishlists;

UPDATE love_location
SET last_love_at = (
	SELECT love.happened_at FROM love
	WHERE love_location.id = love.love_location_id
	ORDER BY happened_at DESC
	LIMIT 1
);

UPDATE love_location
SET last_active_at = last_love_at;
