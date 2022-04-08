package com.smackmap.smackmapbackend.smacker

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne

@Entity
data class Smacker(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(name = "username")
    var userName: String,

    @Column(name = "email")
    var email: String,

    @Column(name = "link")
    var link: String? = null,

    @OneToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(name = "partner_id", nullable = true)
    var partner: Smacker? = null
)

enum class PartnershipStatus {
    REQUESTED, WAITING_FOR_YOUR_RESPONSE, LIVE
}