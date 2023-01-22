DROP INDEX idx_relation_target;
CREATE INDEX idx_relation_target_status ON relation (target_id, status);

DROP INDEX idx_relation_relation_status;
CREATE INDEX idx_relation_status_source ON relation (status, source_id);
