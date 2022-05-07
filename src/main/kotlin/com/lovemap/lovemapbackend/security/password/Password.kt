package com.lovemap.lovemapbackend.security.password

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column

data class Password(
    @Id
    var id: Long = 0,

    @Column("password_hash")
    var passwordHash: String,

    @Column("lover_id")
    var loverId: Long
)
