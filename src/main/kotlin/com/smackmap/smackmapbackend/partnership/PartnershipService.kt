package com.smackmap.smackmapbackend.partnership

import com.smackmap.smackmapbackend.smacker.SmackerService
import org.springframework.stereotype.Service

@Service
class PartnershipService(
    private val smackerService: SmackerService,
    private val partnershipRepository: PartnershipRepository
) {
    suspend fun getSmackerPartnerships(smackerId: Long): SmackerPartnerships {
        val smacker = smackerService.getById(smackerId)

        TODO("Not yet implemented")
    }

    suspend fun requestPartnership(request: RequestPartnershipRequest): Partnership {
        TODO("Not yet implemented")
    }

    suspend fun respondPartnership(request: RespondPartnershipRequest): Partnership {
        TODO("Not yet implemented")
    }

}