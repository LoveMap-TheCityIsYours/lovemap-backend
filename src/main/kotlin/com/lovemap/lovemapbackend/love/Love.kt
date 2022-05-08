package com.lovemap.lovemapbackend.love

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.sql.Timestamp

data class Love(
    @Id
    var id: Long = 0,

    @Column("name")
    var name: String,

    @Column("love_location_id")
    var loveSpotId: Long,

    @Column("lover_id")
    var loverId: Long,

    @Column("happened_at")
    var happenedAt: Timestamp,

    @Column("lover_partner_id")
    var loverPartnerId: Long? = null,

    @Column("note")
    var note: String? = null,
)
