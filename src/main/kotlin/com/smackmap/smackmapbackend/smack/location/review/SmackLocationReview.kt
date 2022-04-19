package com.smackmap.smackmapbackend.smack.location.review

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column

data class SmackLocationReview(
    @Id
    var id: Long = 0,

    @Column("smack_id")
    var smackId: Long,

    @Column("reviewer_id")
    var reviewerId: Long,

    @Column("smack_location_id")
    var smackLocationId: Long,

    @Column("review_text")
    var reviewText: String,

    @Column("review_stars")
    var reviewStars: Int,
)