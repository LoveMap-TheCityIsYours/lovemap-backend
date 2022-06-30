package com.lovemap.lovemapbackend.debug

import com.lovemap.lovemapbackend.lovespot.*
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Profile("dev")
@RestController
@RequestMapping("/debug")
class DebugController(
    private val loveSpotSearchService: LoveSpotSearchService
) {
    @PostMapping("/advancedSearch")
    suspend fun advancedSearch(
        @RequestParam(name = "searchType", required = true) searchResultOrdering: SearchResultOrdering,
        @RequestParam(name = "searchLocation", required = true) searchLocation: SearchLocation,
        @RequestBody request: LoveSpotSearchRequest
    ): ResponseEntity<List<LoveSpotDto>> {
        val loveSpots = loveSpotSearchService.search(searchResultOrdering, searchLocation, request)
        return ResponseEntity.ok(loveSpots)
    }
}