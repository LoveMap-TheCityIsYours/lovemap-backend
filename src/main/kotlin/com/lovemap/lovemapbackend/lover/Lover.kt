package com.lovemap.lovemapbackend.lover

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.sql.Timestamp

data class Lover(
    @Id
    var id: Long = 0,

    @Column("username")
    var userName: String,

    @Column("email")
    var email: String,

    @Column("display_name")
    var displayName: String,

    @Column("registration_country")
    var registrationCountry: String,

    @Column("created_at")
    var createdAt: Timestamp,

    @Column("link")
    var uuid: String? = null,

    @Column("rank")
    var rank: Int = 1,

    @Column("points")
    var points: Int = 0,

    @Column("number_of_loves")
    var numberOfLoves: Int = 0,

    @Column("reviews_submitted")
    var reviewsSubmitted: Int = 0,

    @Column("reports_submitted")
    var reportsSubmitted: Int = 0,

    @Column("reports_received")
    var reportsReceived: Int = 0,

    @Column("love_spots_added")
    var loveSpotsAdded: Int = 0,

    @Column("number_of_followers")
    var numberOfFollowers: Int = 0,

    @Column("photos_uploaded")
    var photosUploaded: Int = 0,

    @Column("photo_likes_received")
    var photoLikesReceived: Int = 0,

    @Column("photo_dislikes_received")
    var photoDislikesReceived: Int = 0,

    @Column("partner_id")
    var partnerId: Long? = null
) {
    fun toView() = LoverView(
        id = id,
        partnerId = partnerId,
        displayName = displayName,
        points = points,
        rank = rank,
        createdAt = createdAt,
    )
}

data class LoverView(
    val id: Long,
    val displayName: String,
    val partnerId: Long?,
    val points: Int,
    val rank: Int,
    val createdAt: Timestamp,
)
