ALTER TABLE lover ADD COLUMN partner_id BIGINT NULL DEFAULT NULL;

UPDATE lover
SET partner_id = subquery.partner_id
FROM (
		SELECT lover.id AS lover_id, partnership.respondent_id AS partner_id
		FROM partnership
		JOIN lover ON partnership.initiator_id = lover.id
) AS subquery
WHERE subquery.lover_id = lover.id;

UPDATE lover
SET partner_id = subquery.partner_id
FROM (
		SELECT lover.id AS lover_id, partnership.initiator_id AS partner_id
		FROM partnership
		JOIN lover ON partnership.respondent_id = lover.id
) AS subquery
WHERE subquery.lover_id = lover.id;
