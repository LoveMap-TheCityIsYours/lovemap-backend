package com.smackmap.smackmapbackend.smacker

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/smacker")
class SmackerController(
    private val smackerService: SmackerService,
) {

    @GetMapping
    suspend fun test() = ResponseEntity.ok("banan")

    @GetMapping("/{smackerId}")
    suspend fun getSmacker(@PathVariable smackerId: Long): ResponseEntity<SmackerResponse> {
        return ResponseEntity.ok(SmackerResponse.of(smackerService.getById(smackerId)))
    }

    @GetMapping("/byLink")
    suspend fun getSmackerByLink(@RequestBody request: GetSmackerByLinkRequest)
            : ResponseEntity<SmackerResponse> {
        val link = request.smackerLink
        val smacker = smackerService.getByLink(link)
        return ResponseEntity.ok(SmackerResponse.of(smacker))
    }

    @PutMapping("/generateLink")
    suspend fun generateSmackerLink(@RequestBody request: GenerateSmackerLinkRequest)
            : ResponseEntity<SmackerLinkResponse> {
        val link = smackerService.generateSmackerLink(request.smackerId)
        return ResponseEntity.ok(SmackerLinkResponse(request.smackerId, link))
    }

    @PutMapping("/requestPartnership")
    suspend fun requestPartnership(@RequestBody request: RequestPartnershipRequest)
            : ResponseEntity<SmackerResponse> {
//        smackerService.requestPartnership(request)
        TODO("Not yet implemented")
    }

    @PutMapping("/respondPartnership")
    suspend fun acceptPartnership(@RequestBody request: RespondPartnershipRequest)
            : ResponseEntity<SmackerResponse> {

        TODO("Not yet implemented")
    }
}