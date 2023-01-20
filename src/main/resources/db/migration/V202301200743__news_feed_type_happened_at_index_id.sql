CREATE INDEX news_feed_item_type_happened_at ON news_feed_item (type, happened_at);

CREATE INDEX lover_public_profile_id ON lover (public_profile, id);
