package com.smackmap.smackmapbackend.partnership

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.sql.Timestamp

data class Partnership(
    @Id
    var id: Long,

    @Column("partnership_status")
    var partnershipStatus: PartnershipStatus,

    @Column("requestor_id")
    var requestorId: Long,

    @Column("requestee_id")
    var requesteeId: Long,

    @Column("start_date")
    var startDate: Timestamp?,

    @Column("end_date")
    var endDate: Timestamp?
)

enum class PartnershipStatus {
    REQUESTED, LIVE, ENDED
}

data class SmackerPartnerships(
    val smackerId: Long,
    val partnerships: List<Partnership>
)
