package com.smackmap.smackmapbackend.smack.location

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.smackmap.smackmapbackend.smack.location.SmackLocation.Availability.ALL_DAY
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalTime

data class SmackLocation(
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

    @Column("custom_availability")
    var customAvailability: String? = null,

    @Column("availability")
    var availability: Availability = ALL_DAY,

    @Column("average_danger")
    var averageDanger: Double? = null,

    @Column("number_of_ratings")
    var numberOfRatings: Int = 0,

    @Column("average_rating")
    var averageRating: Double? = null,
) {
    enum class Availability {
        ALL_DAY, NIGHT_ONLY
    }

    fun readCustomAvailability(): Pair<LocalTime, LocalTime> {
        return objectMapper.readValue(customAvailability, object : TypeReference<Pair<LocalTime, LocalTime>>() {})
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
