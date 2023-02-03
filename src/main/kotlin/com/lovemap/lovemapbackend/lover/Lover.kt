package com.lovemap.lovemapbackend.lover

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.sql.Timestamp

@Table
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

    @Column("number_of_followings")
    var numberOfFollowings: Int = 0,

    @Column("hall_of_fame_position")
    var hallOfFamePosition: Int? = null,

    @Column("photos_uploaded")
    var photosUploaded: Int = 0,

    @Column("photo_likes_received")
    var photoLikesReceived: Int = 0,

    @Column("photo_dislikes_received")
    var photoDislikesReceived: Int = 0,

    @Column("partner_id")
    var partnerId: Long? = null,

    @Column("public_profile")
    var publicProfile: Boolean = false,

    @Column("firebase_token")
    var firebaseToken: String? = null,

    @Column("has_firebase_token")
    var hasFirebaseToken: Boolean = false,

) {
    fun toView() = LoverView(
        id = id,
        partnerId = partnerId,
        displayName = displayName,
        points = points,
        rank = rank,
        numberOfFollowers = numberOfFollowers,
        numberOfFollowings = numberOfFollowings,
        hallOfFamePosition = hallOfFamePosition,
        createdAt = createdAt,
    )
}

data class LoverView(
    val id: Long,
    val displayName: String,
    val partnerId: Long?,
    val points: Int,
    val rank: Int,
    val numberOfFollowers: Int,
    val numberOfFollowings: Int,
    val hallOfFamePosition: Int?,
    val createdAt: Timestamp,
)
