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
