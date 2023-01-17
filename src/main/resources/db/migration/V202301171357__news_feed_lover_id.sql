ALTER TABLE news_feed_item ADD COLUMN lover_id BIGINT NOT NULL DEFAULT 1;

CREATE INDEX news_feed_item_lover_id ON news_feed_item (lover_id, happened_at);
