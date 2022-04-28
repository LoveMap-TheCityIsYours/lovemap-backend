package com.smackmap.smackmapbackend.smackspot.review

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("smack_location_review")
data class SmackSpotReview(
    @Id
    var id: Long = 0,

    @Column("smack_id")
    var smackId: Long,

    @Column("reviewer_id")
    var reviewerId: Long,

    @Column("smack_location_id")
    var smackSpotId: Long,

    @Column("review_text")
    var reviewText: String,

    @Column("review_stars")
    var reviewStars: Int,
)