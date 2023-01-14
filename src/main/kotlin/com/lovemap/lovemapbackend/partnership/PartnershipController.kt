package com.lovemap.lovemapbackend.partnership

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Deprecated("Not very nice REST API")
@RestController
@RequestMapping("/partnerships")
class PartnershipController(
    private val partnershipService: PartnershipService
) {
    @GetMapping("/{loverId}")
    suspend fun getLoverPartnerships(@PathVariable("loverId") loverId: Long)
            : ResponseEntity<LoverPartnershipsResponse> {
        val loverPartnerships: LoverPartnerships = partnershipService.getLoverPartnerships(loverId)
        return ResponseEntity.ok(LoverPartnershipsResponse.of(loverPartnerships))
    }

    @PutMapping("requestPartnership")
    suspend fun requestPartnership(@RequestBody request: RequestPartnershipRequest)
            : ResponseEntity<PartnershipResponse> {
        val partnership: Partnership = partnershipService.requestPartnership(request)
        return ResponseEntity.ok(PartnershipResponse.of(partnership))
    }

    @PutMapping("respondPartnership")
    suspend fun respondPartnership(@RequestBody request: RespondPartnershipRequest)
            : ResponseEntity<LoverPartnershipsResponse> {
        val loverPartnerships: LoverPartnerships = partnershipService.respondToPartnershipRequest(request)
        return ResponseEntity.ok(LoverPartnershipsResponse.of(loverPartnerships))
    }
}
