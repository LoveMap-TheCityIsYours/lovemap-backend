ALTER TABLE love_location ADD COLUMN description TEXT NOT NULL DEFAULT 'NICE';
ALTER TABLE love_location ADD COLUMN availability VARCHAR(255) NOT NULL DEFAULT 'ALL_DAY';
ALTER TABLE love_location ADD COLUMN custom_availability VARCHAR(255) NULL;
ALTER TABLE love_location ADD COLUMN added_by BIGINT REFERENCES lover (id) NOT NULL DEFAULT 1;
ALTER TABLE love_location ADD COLUMN number_of_reports INTEGER NOT NULL DEFAULT 0;
ALTER TABLE love_location ADD COLUMN average_danger FLOAT NULL;

CREATE INDEX idx_love_location_added_by ON love_location (added_by);
CREATE INDEX idx_love_location_average_danger ON love_location (average_danger);


ALTER TABLE love_location_review ADD COLUMN danger_level SMALLINT NOT NULL DEFAULT 3;
ALTER TABLE love_location_review ALTER COLUMN review_stars TYPE SMALLINT;

CREATE TABLE love_location_report
(
    id            			BIGSERIAL	 	PRIMARY KEY,
    report_text				TEXT 			NOT NULL,
    lover_id    			BIGINT 			REFERENCES lover (id) NOT NULL,
    love_location_id    	BIGINT 			REFERENCES love_location (id) NOT NULL
);
CREATE INDEX idx_love_location_report_lover_id ON love_location_report (lover_id);
CREATE INDEX idx_love_location_report_love_location_id ON love_location_report (love_location_id);
