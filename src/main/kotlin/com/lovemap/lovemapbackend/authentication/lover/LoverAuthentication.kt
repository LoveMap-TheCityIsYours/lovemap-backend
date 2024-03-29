package com.lovemap.lovemapbackend.authentication.lover

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.sql.Timestamp

@Table
data class LoverAuthentication(
    @Id
    var id: Long = 0,

    @Column("password_hash")
    var passwordHash: String?,

    @Column("lover_id")
    var loverId: Long,

    @Column("reset_code")
    var resetCode: String? = null,

    @Column("reset_initiated_at")
    var resetInitiatedAt: Timestamp? = null,

    @Column("password_set")
    var passwordSet: Boolean = true,

    @Column("facebook_id")
    var facebookId: String? = null,
)
