package com.smackmap.smackmapbackend.partnership

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/partnership")
class PartnershipController(
    private val partnershipService: PartnershipService
) {
    @GetMapping
    suspend fun getSmackerPartnerships(
        @RequestParam("smackerId", required = true) smackerId: Long
    ): ResponseEntity<SmackerPartnershipsResponse> {
        val smackerPartnerships: SmackerPartnerships = partnershipService.getSmackerPartnerships(smackerId)
        return ResponseEntity.ok(SmackerPartnershipsResponse.of(smackerPartnerships))
    }

    @PutMapping("requestPartnership")
    suspend fun requestPartnership(@RequestBody request: RequestPartnershipRequest)
            : ResponseEntity<PartnershipResponse> {
        val partnership: Partnership = partnershipService.requestPartnership(request)
        return ResponseEntity.ok(PartnershipResponse.of(partnership))
    }

    @PutMapping("respondPartnership")
    suspend fun respondPartnership(@RequestBody request: RespondPartnershipRequest)
            : ResponseEntity<SmackerPartnershipsResponse> {
        val smackerPartnerships: SmackerPartnerships = partnershipService.respondToPartnershipRequest(request)
        return ResponseEntity.ok(SmackerPartnershipsResponse.of(smackerPartnerships))
    }
}