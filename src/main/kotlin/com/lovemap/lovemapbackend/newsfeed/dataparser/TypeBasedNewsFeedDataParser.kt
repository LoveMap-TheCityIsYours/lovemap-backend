package com.lovemap.lovemapbackend.newsfeed.dataparser

import com.fasterxml.jackson.databind.ObjectMapper
import com.lovemap.lovemapbackend.newsfeed.data.*
import org.springframework.stereotype.Component

interface TypeBasedNewsFeedDataParser<out ComparableNewsFeedData> {
    fun supportedType(): NewsFeedItem.Type
    fun parse(data: String): ComparableNewsFeedData
}

@Component
class LoveNewsFeedDataParser(
    private val objectMapper: ObjectMapper
) : TypeBasedNewsFeedDataParser<LoveNewsFeedData> {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE
    }

    override fun parse(data: String): LoveNewsFeedData {
        return objectMapper.readValue(data, LoveNewsFeedData::class.java)
    }
}

@Component
class LoverNewsFeedDataParser(
    private val objectMapper: ObjectMapper
) : TypeBasedNewsFeedDataParser<LoverNewsFeedData> {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVER
    }

    override fun parse(data: String): LoverNewsFeedData {
        return objectMapper.readValue(data, LoverNewsFeedData::class.java)
    }
}

@Component
class LoveSpotNewsFeedDataParser(
    private val objectMapper: ObjectMapper
) : TypeBasedNewsFeedDataParser<LoveSpotNewsFeedData> {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT
    }

    override fun parse(data: String): LoveSpotNewsFeedData {
        return objectMapper.readValue(data, LoveSpotNewsFeedData::class.java)
    }
}

@Component
class LoveSpotPhotoNewsFeedDataParser(
    private val objectMapper: ObjectMapper
) : TypeBasedNewsFeedDataParser<LoveSpotPhotoNewsFeedData> {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT_PHOTO
    }

    override fun parse(data: String): LoveSpotPhotoNewsFeedData {
        return objectMapper.readValue(data, LoveSpotPhotoNewsFeedData::class.java)
    }
}

@Component
class LoveSpotPhotoLikeNewsFeedDataParser(
    private val objectMapper: ObjectMapper
) : TypeBasedNewsFeedDataParser<PhotoLikeNewsFeedData> {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT_PHOTO_LIKE
    }

    override fun parse(data: String): PhotoLikeNewsFeedData {
        return objectMapper.readValue(data, PhotoLikeNewsFeedData::class.java)
    }
}

@Component
class LoveSpotReviewNewsFeedDataParser(
    private val objectMapper: ObjectMapper
) : TypeBasedNewsFeedDataParser<LoveSpotReviewNewsFeedData> {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.LOVE_SPOT_REVIEW
    }

    override fun parse(data: String): LoveSpotReviewNewsFeedData {
        return objectMapper.readValue(data, LoveSpotReviewNewsFeedData::class.java)
    }
}

@Component
class WishlistNewsFeedDataParser(
    private val objectMapper: ObjectMapper
) : TypeBasedNewsFeedDataParser<WishlistNewsFeedData> {

    override fun supportedType(): NewsFeedItem.Type {
        return NewsFeedItem.Type.WISHLIST_ITEM
    }

    override fun parse(data: String): WishlistNewsFeedData {
        return objectMapper.readValue(data, WishlistNewsFeedData::class.java)
    }
}



