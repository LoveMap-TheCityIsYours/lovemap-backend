package com.lovemap.lovemapbackend.partnership

import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v2/lovers/{loverId}/partnerships")
class PartnershipV2Controller(
    private val partnershipService: PartnershipService
) {

    @GetMapping
    suspend fun getLoverPartnership(@PathVariable("loverId") loverId: Long): LoverPartnershipV2Response {
        val loverPartnerships: LoverPartnerships = partnershipService.getLoverPartnerships(loverId)
        return LoverPartnershipV2Response.of(loverPartnerships)
    }

    @PutMapping("requestPartnership")
    suspend fun requestPartnership(
        @PathVariable("loverId") loverId: Long,
        @RequestBody request: RequestPartnershipRequest
    ): PartnershipResponse {
        if (loverId != request.initiatorId) {
            throw LoveMapException(HttpStatus.BAD_REQUEST, ErrorCode.Forbidden)
        }
        val partnership: Partnership = partnershipService.requestPartnership(request)
        return PartnershipResponse.of(partnership)
    }

    @PutMapping("respondPartnership")
    suspend fun respondPartnership(
        @PathVariable("loverId") loverId: Long,
        @RequestBody request: RespondPartnershipRequest
    ): LoverPartnershipV2Response {
        if (loverId != request.respondentId) {
            throw LoveMapException(HttpStatus.BAD_REQUEST, ErrorCode.Forbidden)
        }
        val loverPartnerships: LoverPartnerships = partnershipService.respondToPartnershipRequest(request)
        return LoverPartnershipV2Response.of(loverPartnerships)
    }

    @PutMapping("cancelPartnershipRequest")
    suspend fun cancelPartnershipRequest(
        @PathVariable("loverId") loverId: Long,
        @RequestBody request: CancelPartnershipRequest
    ): LoverPartnershipV2Response {
        if (loverId != request.initiatorId) {
            throw LoveMapException(HttpStatus.BAD_REQUEST, ErrorCode.Forbidden)
        }
        val loverPartnerships: LoverPartnerships = partnershipService.cancelPartnershipRequest(loverId, request)
        return LoverPartnershipV2Response.of(loverPartnerships)
    }

    @PutMapping("endPartnership/{partnerLoverId}")
    suspend fun endPartnership(
        @PathVariable("loverId") loverId: Long,
        @PathVariable("partnerLoverId") partnerLoverId: Long
    ): LoverPartnershipV2Response {
        val loverPartnerships: LoverPartnerships = partnershipService.endPartnership(loverId, partnerLoverId)
        return LoverPartnershipV2Response.of(loverPartnerships)
    }
}