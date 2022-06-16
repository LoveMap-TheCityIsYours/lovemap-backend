package com.lovemap.lovemapbackend.authentication.password

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.sql.Timestamp

data class Password(
    @Id
    var id: Long = 0,

    @Column("password_hash")
    var passwordHash: String,

    @Column("lover_id")
    var loverId: Long,

    @Column("reset_code")
    var resetCode: String? = null,

    @Column("reset_initiated_at")
    var resetInitiatedAt: Timestamp? = null,
)
