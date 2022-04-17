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
) {
    fun toView() = SmackerView(id, userName)
}

data class SmackerView(
    val id: Long,
    val userName: String
)
