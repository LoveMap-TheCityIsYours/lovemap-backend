ALTER TABLE lover 					ADD COLUMN 		photos_uploaded 	INT NOT NULL DEFAULT 0;
ALTER TABLE love_location 			ADD COLUMN 		number_of_photos 	INT NOT NULL DEFAULT 0;
ALTER TABLE love_location_review 	ADD COLUMN 		number_of_photos 	INT NOT NULL DEFAULT 0;

CREATE TABLE love_location_photo
(
    id             				BIGSERIAL 		PRIMARY KEY,
    url           				VARCHAR(512) 	NOT NULL,
    love_location_id  			BIGINT 			REFERENCES love_location (id) NOT NULL,
    uploaded_by  				BIGINT 			REFERENCES lover (id) NOT NULL,
    love_location_review_id  	BIGINT 			REFERENCES love_location_review (id) NULL,
    likes       				BIGINT 			NOT NULL DEFAULT 0,
    dislikes       				BIGINT 			NOT NULL DEFAULT 0
);
CREATE INDEX idx_love_location_photo_location_id ON love_location_photo (love_location_id);
CREATE INDEX idx_love_location_photo_location_review_id ON love_location_photo (love_location_review_id, love_location_id);
CREATE INDEX idx_love_location_photo_uploaded_by ON love_location_photo (uploaded_by, love_location_id);
CREATE INDEX idx_love_location_photo_uploaded_by_review ON love_location_photo (love_location_review_id, uploaded_by);
