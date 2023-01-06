CREATE INDEX idx_wishlist_element_love_location_id ON wishlist_element (love_location_id);

UPDATE love_location
SET occurrence_on_wishlists = subquery.occurrence
FROM (
		SELECT wishlist_element.love_location_id AS love_location_id, COUNT(love_location_id) AS occurrence
		FROM wishlist_element
		JOIN love_location ON love_location.id = wishlist_element.love_location_id
		GROUP BY wishlist_element.love_location_id
) AS subquery
WHERE subquery.love_location_id = love_location.id;
