package com.smackmap.smackmapbackend.smackspot.report

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("smack_location_report")
data class SmackSpotReport(
    @Id
    var id: Long = 0,

    @Column("smacker_id")
    var smackerId: Long,

    @Column("smack_location_id")
    var smackSpotId: Long,

    @Column("report_text")
    var reportText: String,
)