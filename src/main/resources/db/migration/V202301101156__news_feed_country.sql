ALTER TABLE news_feed_item ADD COLUMN country VARCHAR(255) NOT NULL DEFAULT 'GLOBAL';

CREATE INDEX news_feed_item_country ON news_feed_item (country, happened_at);
