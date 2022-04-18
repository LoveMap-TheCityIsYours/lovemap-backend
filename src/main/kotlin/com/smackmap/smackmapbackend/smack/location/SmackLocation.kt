package com.smackmap.smackmapbackend.smack.location

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column

data class SmackLocation(
    @Id
    var id: Long = 0,

    @Column("name")
    var name: String,

    @Column("longitude")
    var longitude: Double,

    @Column("latitude")
    var latitude: Double,

    @Column("average_rating")
    var averageRating: Double? = null
)
