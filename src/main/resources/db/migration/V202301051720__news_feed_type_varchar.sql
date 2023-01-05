DROP INDEX news_feed_item_type_reference_id;
ALTER TABLE news_feed_item DROP COLUMN type;
ALTER TABLE news_feed_item ADD COLUMN type TEXT NOT NULL;
CREATE INDEX news_feed_item_type_reference_id ON news_feed_item (type, reference_id);
