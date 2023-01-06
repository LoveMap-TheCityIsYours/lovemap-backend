ALTER TABLE news_feed_item ADD COLUMN happened_at TIMESTAMP NOT NULL;

CREATE INDEX news_feed_item_happened_at ON news_feed_item (happened_at);
