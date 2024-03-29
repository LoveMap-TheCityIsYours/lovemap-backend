package com.lovemap.lovemapbackend.lovespot

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.lovemap.lovemapbackend.lovespot.LoveSpot.Availability.ALL_DAY
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.sql.Timestamp
import java.time.LocalTime

@Table("love_location")
data class LoveSpot(
    @Id
    var id: Long = 0,

    @Column("name")
    var name: String,

    @Column("longitude")
    var longitude: Double,

    @Column("latitude")
    var latitude: Double,

    @Column("description")
    var description: String,

    @Column("added_by")
    var addedBy: Long,

    @Column("number_of_reports")
    var numberOfReports: Int = 0,

    @Column("type")
    var type: Type = Type.PUBLIC_SPACE,

    @Column("availability")
    var availability: Availability = ALL_DAY,

    @Column("custom_availability")
    var customAvailability: String? = null,

    @Column("average_danger")
    var averageDanger: Double? = null,

    @Column("number_of_ratings")
    var numberOfRatings: Int = 0,

    @Column("average_rating")
    var averageRating: Double? = null,

    @Column("geo_location_id")
    var geoLocationId: Long? = null,

    @Column("number_of_loves")
    var numberOfLoves: Long = 0,

    @Column("number_of_comments")
    var numberOfComments: Long = 0,

    @Column("occurrence_on_wishlists")
    var occurrenceOnWishlists: Long = 0,

    @Column("popularity")
    var popularity: Long = 0,

    @Column("created_at")
    var createdAt: Timestamp,

    @Column("last_comment_at")
    var lastCommentAt: Timestamp? = null,

    @Column("last_love_at")
    var lastLoveAt: Timestamp? = null,

    @Column("last_active_at")
    var lastActiveAt: Timestamp? = null,

    @Column("last_photo_added_at")
    var lastPhotoAddedAt: Timestamp? = null,

    @Column("number_of_photos")
    var numberOfPhotos: Int = 0
) {
    enum class Availability {
        ALL_DAY, NIGHT_ONLY
    }

    enum class Type {
        PUBLIC_SPACE,
        SWINGER_CLUB,
        CRUISING_SPOT,
        SEX_BOOTH,
        NIGHT_CLUB,
        OTHER_VENUE
    }

    fun readCustomAvailability(): Pair<LocalTime, LocalTime>? {
        customAvailability?.let {
            return objectMapper.readValue(it, object : TypeReference<Pair<LocalTime, LocalTime>>() {})
        }
        return null
    }

    fun setCustomAvailability(fromTo: Pair<LocalTime, LocalTime>?) {
        if (fromTo != null) {
            customAvailability = objectMapper.writeValueAsString(fromTo)
        }
    }

    companion object {
        private val objectMapper = ObjectMapper()
    }
}
