package com.smackmap.smackmapbackend.smack.location.report

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column

data class SmackLocationReport(
    @Id
    var id: Long = 0,

    @Column("smacker_id")
    var smackerId: Long,

    @Column("smack_location_id")
    var smackLocationId: Long,

    @Column("report_text")
    var reportText: String,
)