package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.lover.ranks.LoverRanks
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import com.lovemap.lovemapbackend.utils.ValidatorService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lovers")
class LoverController(
    private val loverRelationService: LoverRelationService,
    private val loverService: LoverService,
    private val cachedLoverService: CachedLoverService,
    private val loverContributionsService: LoverContributionsService,
    private val loverConverter: LoverConverter,
    private val loverRanks: LoverRanks,
    private val validatorService: ValidatorService
) {
    @GetMapping("/contributions/{loverId}")
    suspend fun contributions(@PathVariable loverId: Long): LoverContributionsResponse {
        return loverContributionsService.list(loverId)
    }

    @GetMapping("/{loverId}")
    suspend fun getLover(@PathVariable loverId: Long): LoverRelationsResponse {
        return loverRelationService.getWithRelations(loverId)
    }

    @PutMapping("/{loverId}")
    suspend fun updateLover(
        @PathVariable loverId: Long,
        @RequestBody updateLoverRequest: UpdateLoverRequest
    ): LoverResponse {
        validatorService.validate(updateLoverRequest)
        return loverService.updateLover(loverId, updateLoverRequest)
    }

    @GetMapping("/view/{loverId}")
    suspend fun getLoverView(@PathVariable loverId: Long): LoverViewResponse {
        return loverRelationService.getById(loverId)
    }

    @GetMapping("/cachedView/{loverId}")
    suspend fun getCachedLoverView(@PathVariable loverId: Long): LoverViewWithoutRelationResponse {
        return cachedLoverService.getCachedLoverById(loverId) ?: throw LoveMapException(
            HttpStatus.NOT_FOUND,
            ErrorCode.LoverNotFound
        )
    }

    @GetMapping
    suspend fun getLoverByUuid(@RequestParam uuid: String): LoverViewResponse {
        return loverRelationService.getByUuid(uuid)
    }

    @PostMapping("/{loverId}/shareableLink")
    suspend fun generateLoverLink(@PathVariable loverId: Long): LoverResponse {
        val lover = loverService.generateLoverUuid(loverId)
        return loverConverter.toResponse(lover)
    }

    @DeleteMapping("/{loverId}/shareableLink")
    suspend fun deleteLoverLink(@PathVariable loverId: Long): LoverResponse {
        val lover = loverService.deleteLoverLink(loverId)
        return loverConverter.toResponse(lover)
    }

    @GetMapping("ranks")
    suspend fun getRanks(): LoverRanks {
        return loverRanks
    }
}