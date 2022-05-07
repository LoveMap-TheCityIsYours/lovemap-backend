package com.lovemap.lovemapbackend.partnership

import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.sql.Timestamp

data class Partnership(
    @Id
    var id: Long = 0,

    @Column("status")
    var status: Status,

    @Column("initiator_id")
    var initiatorId: Long,

    @Column("respondent_id")
    var respondentId: Long,

    @Column("initiate_date")
    var initiateDate: Timestamp? = null,

    @Column("respond_date")
    var respondDate: Timestamp? = null,

    @Column("end_date")
    var endDate: Timestamp? = null
) {
    enum class Status {
        PARTNERSHIP_REQUESTED, PARTNER
    }
}

data class LoverPartnerships(
    val loverId: Long,
    val relations: Flow<Partnership>
)
