CREATE TABLE photo_like
(
    id             			BIGSERIAL 		PRIMARY KEY,
    photo_id 		 		BIGINT 			REFERENCES love_location_photo (id) NOT NULL,
    lover_id  				BIGINT 			REFERENCES lover (id) NOT NULL,
    happened_at 			TIMESTAMP		NOT NULL,
    like_or_dislike			SMALLINT		NOT NULL
);

CREATE UNIQUE INDEX idx_photo_like_photo_id_lover_id ON photo_like (photo_id, lover_id);
CREATE INDEX idx_photo_like_lover_id_like_or_dislike ON photo_like (lover_id, like_or_dislike);
CREATE INDEX idx_photo_like_happened_at ON photo_like (happened_at);

CREATE TABLE photo_likers_dislikers
(
	id             			BIGSERIAL 		PRIMARY KEY,
	photo_id 		 		BIGINT 			REFERENCES love_location_photo (id) NOT NULL,
	likers 					TEXT			DEFAULT NULL,
	dislikers 				TEXT			DEFAULT NULL
);

CREATE INDEX idx_photo_likers_dislikers_photo_id ON photo_likers_dislikers (photo_id);
