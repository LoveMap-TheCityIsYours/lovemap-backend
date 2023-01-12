ALTER TABLE lover ADD COLUMN display_name VARCHAR(255) NOT NULL DEFAULT 'not-set-yet';
ALTER TABLE lover ADD COLUMN registration_country VARCHAR(255) NOT NULL DEFAULT 'GLOBAL';

CREATE INDEX lover_registration_country ON lover (registration_country);
CREATE INDEX lover_display_name ON lover (display_name);

UPDATE lover
SET display_name = username
WHERE username <> email;
