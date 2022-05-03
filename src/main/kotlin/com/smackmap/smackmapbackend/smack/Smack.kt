package com.smackmap.smackmapbackend.smack

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column

data class Smack(
    @Id
    var id: Long = 0,

    @Column("name")
    var name: String,

    @Column("smack_location_id")
    var smackSpotId: Long,

    @Column("smacker_id")
    var smackerId: Long,

    @Column("smacker_partner_id")
    var smackerPartnerId: Long? = null,

    @Column("note")
    var note: String? = null,
)
