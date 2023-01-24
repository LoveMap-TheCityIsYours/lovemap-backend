ALTER TABLE lover ADD COLUMN number_of_followings INTEGER NOT NULL DEFAULT 0;
ALTER TABLE lover ADD COLUMN hall_of_fame_position INTEGER DEFAULT NULL;

CREATE INDEX idx_lover_points ON lover (points);

ALTER TABLE relation ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW();

CREATE INDEX idx_relation_followingstatus_source_created_at
	ON relation (status, source_id, created_at)
	WHERE status = 'FOLLOWING';

CREATE INDEX idx_relation_followingstatus_target_created_at
	ON relation (status, target_id, created_at)
	WHERE status = 'FOLLOWING';
