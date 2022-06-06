package com.lovemap.lovemapbackend.lovespot

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.lovemap.lovemapbackend.geolocation.GeoLocation
import com.lovemap.lovemapbackend.lovespot.LoveSpot.Availability.ALL_DAY
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
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
