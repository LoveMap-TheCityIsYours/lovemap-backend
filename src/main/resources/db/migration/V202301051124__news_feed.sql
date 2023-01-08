ALTER TABLE love_location ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW()
	- (6 + (RANDOM() * 180)::INT) * INTERVAL '1 day'
	- (RANDOM() * 600)::INT * INTERVAL '1 minute'
	- (RANDOM() * 60)::INT * INTERVAL '1 second';

ALTER TABLE love_location_review ADD COLUMN submitted_at TIMESTAMP NOT NULL DEFAULT NOW();

UPDATE love_location_review llr SET submitted_at = l.happened_at
FROM love l
WHERE llr.love_id = l.id;

CREATE INDEX love_location_created_at ON love_location (created_at);
CREATE INDEX love_happened_at ON love (happened_at);
CREATE INDEX love_location_photo_uploaded_at ON love_location_photo (uploaded_at);
CREATE INDEX wishlist_element_added_at ON wishlist_element (added_at);
CREATE INDEX lover_created_at ON lover (created_at);
CREATE INDEX love_location_review_submitted_at ON love_location_review (submitted_at);

CREATE TYPE news_feed_item_type AS ENUM (
	'LOVE_SPOT',
	'LOVE_SPOT_REVIEW',
	'LOVE_SPOT_PHOTO',
	'LOVE_SPOT_PHOTO_LIKE_DISLIKE',
	'LOVE',
	'WISHLIST_ITEM',
	'LOVER'
);


CREATE TABLE news_feed_item
(
	id						BIGSERIAL 				PRIMARY KEY,
	generated_at 			TIMESTAMP 				NOT NULL,
	type					news_feed_item_type		NOT NULL,
	data					TEXT					NOT NULL
);
