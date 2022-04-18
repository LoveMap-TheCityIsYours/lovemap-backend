package com.smackmap.smackmapbackend.smack.location

import kotlinx.coroutines.flow.Flow
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/smack/location")
class SmackLocationController(
    private val smackLocationService: SmackLocationService
) {
    @PostMapping
    suspend fun create(@RequestBody request: CreateSmackLocationRequest): ResponseEntity<SmackLocationDto> {
        val smackLocation = smackLocationService.create(request)
        return ResponseEntity.ok(SmackLocationDto.of(smackLocation))
    }

    @PostMapping("/search")
    suspend fun list(@RequestBody request: SmackLocationSearchRequest): ResponseEntity<Flow<SmackLocationDto>> {
        TODO("finish")
    }
}