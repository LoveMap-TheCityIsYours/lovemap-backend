package com.lovemap.lovemapbackend.lovespot.photo

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.sql.Timestamp

@Table("love_location_photo")
data class LoveSpotPhoto(
    @Id
    var id: Long = 0,

    @Column("url")
    var url: String,

    @Column("love_location_id")
    var loveSpotId: Long,

    @Column("uploaded_by")
    var uploadedBy: Long,

    @Column("uploaded_at")
    var uploadedAt: Timestamp,

    @Column("file_name")
    var fileName: String,

    @Column("love_location_review_id")
    var loveSpotReviewId: Long? = null,

    @Column("likes")
    var likes: Int = 0,

    @Column("dislikes")
    var dislikes: Int = 0,
)
