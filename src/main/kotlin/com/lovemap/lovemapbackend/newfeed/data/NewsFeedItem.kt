package com.lovemap.lovemapbackend.newfeed.data

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.sql.Timestamp

@Table("news_feed_item")
data class NewsFeedItem(
    @Id
    var id: Long = 0,

    @Column("generated_at")
    var generatedAt: Timestamp,

    @Column("happened_at")
    var happenedAt: Timestamp,

    @Column("type")
    var type: Type,

    @Column("reference_id")
    var referenceId: Long,

    @Column("country")
    var country: String = DEFAULT_COUNTRY,

    @Column("data")
    var data: String
) {
    enum class Type {
        LOVE_SPOT,
        LOVE_SPOT_REVIEW,
        LOVE_SPOT_PHOTO,
        LOVE_SPOT_PHOTO_LIKE,
        LOVE,
        WISHLIST_ITEM,
        LOVER
    }

    companion object {
        const val DEFAULT_COUNTRY = "GLOBAL"
    }
}
