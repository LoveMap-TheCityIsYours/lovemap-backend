package com.smackmap.smackmapbackend.security.password

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column

data class Password(
    @Id
    var id: Long = 0,

    @Column("password_hash")
    var passwordHash: String,

    @Column("smacker_id")
    var smackerId: Long
)
