ALTER TABLE lover ADD COLUMN reports_received INT NOT NULL DEFAULT 0;
ALTER TABLE lover ADD COLUMN reviews_submitted INT NOT NULL DEFAULT 0;
ALTER TABLE lover RENAME COLUMN number_of_reports TO reports_submitted;
