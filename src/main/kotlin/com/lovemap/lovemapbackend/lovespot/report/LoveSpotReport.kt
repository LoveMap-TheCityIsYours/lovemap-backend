package com.lovemap.lovemapbackend.lovespot.report

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("love_location_report")
data class LoveSpotReport(
    @Id
    var id: Long = 0,

    @Column("lover_id")
    var loverId: Long,

    @Column("love_location_id")
    var loveSpotId: Long,

    @Column("report_text")
    var reportText: String,
)