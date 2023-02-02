package com.lovemap.lovemapbackend.lover.partnership

import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.http.HttpStatus
import java.sql.Timestamp

@Table
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

    fun partnerOf(loverId: Long): Long {
        return when (loverId) {
            initiatorId -> {
                respondentId
            }
            respondentId -> {
                initiatorId
            }
            else -> {
                throw LoveMapException(HttpStatus.BAD_REQUEST, ErrorCode.PartnershipNotFound)
            }
        }
    }
}

data class LoverPartnership(
    val loverId: Long,
    val partnership: Partnership?
)
