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

    @Column("created_at")
    var createdAt: Timestamp,

    @Column("link")
    var link: String? = null,

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
) {
    fun toView() = LoverView(id, userName, rank)
}

data class LoverView(
    val id: Long,
    val userName: String,
    val rank: Int,
)
