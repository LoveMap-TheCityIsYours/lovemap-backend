CREATE TABLE wishlist_element
(
    id             				BIGSERIAL 		PRIMARY KEY,
    lover_id  					BIGINT 			REFERENCES lover (id) NOT NULL,
    love_location_id  			BIGINT 			REFERENCES love_location (id) NOT NULL,
    added_at				  	TIMESTAMP 		NOT NULL
);

CREATE UNIQUE INDEX idx_wishlist_element_love_location_id_lover_id ON wishlist_element (lover_id, love_location_id);
