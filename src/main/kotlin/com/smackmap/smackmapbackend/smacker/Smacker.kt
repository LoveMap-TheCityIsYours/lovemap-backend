package com.smackmap.smackmapbackend.smacker

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column

data class Smacker(
    @Id
    var id: Long = 0,

    @Column("username")
    var userName: String,

    @Column("email")
    var email: String,

    @Column("link")
    var link: String? = null,
)

enum class PartnershipStatus {
    REQUESTED, WAITING_FOR_YOUR_RESPONSE, LIVE
}