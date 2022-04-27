ALTER TABLE smack_location ADD COLUMN description TEXT NOT NULL DEFAULT 'NICE';
ALTER TABLE smack_location ADD COLUMN availability VARCHAR(255) NOT NULL DEFAULT 'ALL_DAY';
ALTER TABLE smack_location ADD COLUMN custom_availability VARCHAR(255) NULL;
ALTER TABLE smack_location ADD COLUMN added_by BIGINT REFERENCES smacker (id) NOT NULL DEFAULT 1;
ALTER TABLE smack_location ADD COLUMN number_of_reports INTEGER NOT NULL DEFAULT 0;
ALTER TABLE smack_location ADD COLUMN average_danger FLOAT NULL;

CREATE INDEX idx_smack_location_added_by ON smack_location (added_by);
CREATE INDEX idx_smack_location_average_danger ON smack_location (average_danger);


ALTER TABLE smack_location_review ADD COLUMN danger_level SMALLINT NOT NULL DEFAULT 3;
ALTER TABLE smack_location_review ALTER COLUMN review_stars TYPE SMALLINT;

CREATE TABLE smack_location_report
(
    id            			BIGSERIAL	 	PRIMARY KEY,
    report_text				TEXT 			NOT NULL,
    smacker_id    			BIGINT 			REFERENCES smacker (id) NOT NULL,
    smack_location_id    	BIGINT 			REFERENCES smack_location (id) NOT NULL
);
CREATE INDEX idx_smack_location_report_smacker_id ON smack_location_report (smacker_id);
CREATE INDEX idx_smack_location_report_smack_location_id ON smack_location_report (smack_location_id);

