package com.lovemap.lovemapbackend.lovespot.review

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("love_location_review")
data class LoveSpotReview(
    @Id
    var id: Long = 0,

    @Column("love_id")
    var loveId: Long,

    @Column("reviewer_id")
    var reviewerId: Long,

    @Column("love_location_id")
    var loveSpotId: Long,

    @Column("review_text")
    var reviewText: String,

    @Column("review_stars")
    var reviewStars: Int,

    @Column("danger_level")
    var riskLevel: Int,
)