ALTER TABLE news_feed_item ADD COLUMN reference_id BIGINT NOT NULL;

CREATE INDEX news_feed_item_generated_at ON news_feed_item (generated_at);
CREATE INDEX news_feed_item_type_reference_id ON news_feed_item (type, reference_id);
