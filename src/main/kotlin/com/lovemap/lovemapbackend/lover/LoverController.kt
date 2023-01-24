package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.lover.ranking.LoverRanks
import com.lovemap.lovemapbackend.lover.relation.LoverRelationsResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
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
    private val loverActivitiesService: LoverActivitiesService,
    private val cachedLoverService: CachedLoverService,
    private val loverConverter: LoverConverter,
    private val loverRanks: LoverRanks,
    private val validatorService: ValidatorService
) {

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

    @GetMapping("{loverId}/activities")
    suspend fun getLoverActivities(@PathVariable loverId: Long): List<NewsFeedItemResponse> {
        return loverActivitiesService.getActivities(loverId)
    }

    @GetMapping("hallOfFame")
    suspend fun getHallOfFame(): List<LoverViewWithoutRelationResponse> {
        return loverService.getHallOfFame()
    }
}