package com.smackmap.smackmapbackend.smacker

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.annotation.security.RolesAllowed

@RestController
@RolesAllowed("USER")
@RequestMapping("/smacker")
class SmackerController(
    private val smackerService: SmackerService,
) {

    @GetMapping("/{smackerId}")
    fun getSmacker(@PathVariable smackerId: Long): ResponseEntity<SmackerResponse> {
        return ResponseEntity.ok(SmackerResponse.of(smackerService.getById(smackerId)))
    }

    @GetMapping("/byLink")
    fun getSmackerByLink(@RequestBody request: GetSmackerByLinkRequest)
            : ResponseEntity<SmackerResponse> {
        val link = request.smackerLink
        val smacker = smackerService.getByLink(link)
        return ResponseEntity.ok(SmackerResponse.of(smacker))
    }

    @PutMapping("/generateLink")
    fun generateSmackerLink(@RequestBody request: GenerateSmackerLinkRequest)
            : ResponseEntity<SmackerLinkResponse> {
        val link = smackerService.generateSmackerLink(request.smackerId)
        return ResponseEntity.ok(SmackerLinkResponse(request.smackerId, link))
    }

    @PutMapping("/requestPartnership")
    fun requestPartnership(@RequestBody request: RequestPartnershipRequest)
            : ResponseEntity<SmackerResponse> {
//        smackerService.requestPartnership(request)
        TODO("Not yet implemented")
    }

    @PutMapping("/respondPartnership")
    fun acceptPartnership(@RequestBody request: RespondPartnershipRequest)
            : ResponseEntity<SmackerResponse> {

        TODO("Not yet implemented")
    }
}