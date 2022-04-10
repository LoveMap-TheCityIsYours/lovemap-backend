package com.smackmap.smackmapbackend.partnership

import com.smackmap.smackmapbackend.relation.Relation
import com.smackmap.smackmapbackend.relation.SmackerRelations
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/partnership")
class PartnershipController(
    private val partnershipService: PartnershipService
) {
    @GetMapping
    suspend fun getSmackerPartnerships(
        @RequestParam("smackerId", required = true) smackerId: Long
    ): ResponseEntity<SmackerPartnershipsResponse> {
        val smackerRelations: SmackerRelations = partnershipService.getSmackerPartnerships(smackerId)
        return ResponseEntity.ok(SmackerPartnershipsResponse.of(smackerRelations))
    }

    @PutMapping("requestPartnership")
    suspend fun requestPartnership(@RequestBody request: RequestPartnershipRequest)
            : ResponseEntity<PartnershipResponse> {
        val relation: Relation = partnershipService.requestPartnership(request)
        return ResponseEntity.ok(PartnershipResponse.of(relation))
    }

    @PutMapping("respondPartnership")
    suspend fun respondPartnership(@RequestBody request: RespondPartnershipRequest)
            : ResponseEntity<PartnershipResponse> {
        val relation: Relation = partnershipService.respondPartnership(request)
        return ResponseEntity.ok(PartnershipResponse.of(relation))
    }
}