CREATE TABLE news_feed_generation(
	id									BIGSERIAL 				PRIMARY KEY,
	generated_at 						TIMESTAMP 				NOT NULL,
	generated_items						BIGINT					NOT NULL DEFAULT 0,
	generation_duration_milliseconds	BIGINT					NOT NULL DEFAULT 0
);

CREATE INDEX news_feed_generation_generated_at ON news_feed_generation(generated_at);

CREATE UNIQUE INDEX news_feed_item_unique_constraint ON news_feed_item(type, reference_id, happened_at);

INSERT INTO news_feed_generation(generated_at, generated_items)
SELECT generated_at, COUNT(generated_at)
FROM news_feed_item
GROUP BY generated_at;
