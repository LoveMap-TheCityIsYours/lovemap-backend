package com.lovemap.lovemapbackend.lover.relation

import com.lovemap.lovemapbackend.lover.LoverRelationService
import com.lovemap.lovemapbackend.lover.LoverViewWithoutRelationResponse
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/lovers/{loverId}/relations")
class RelationController(
    private val relationService: RelationService,
    private val loverRelationService: LoverRelationService,
) {

    @GetMapping
    suspend fun getRelations(@PathVariable loverId: Long): LoverRelationsResponse {
        return loverRelationService.getWithRelations(loverId)
    }

    @GetMapping("followingNewsFeed")
    suspend fun getFollowingNewsFeed(@PathVariable loverId: Long): List<NewsFeedItemResponse> {
        return relationService.getFollowingNewsFeed(loverId)
    }

    @PostMapping("/{targetLoverId}/follow")
    suspend fun followLover(@PathVariable loverId: Long, @PathVariable targetLoverId: Long): LoverRelationsResponse {
        return relationService.followLover(loverId, targetLoverId)
    }

    @DeleteMapping("/{targetLoverId}/unfollow")
    suspend fun unfollowLover(@PathVariable loverId: Long, @PathVariable targetLoverId: Long): LoverRelationsResponse {
        return relationService.unfollowLover(loverId, targetLoverId)
    }

    @GetMapping("followers")
    suspend fun getFollowers(@PathVariable loverId: Long): List<LoverViewWithoutRelationResponse> {
        return relationService.getFollowers(loverId)
    }

    @DeleteMapping("/{targetLoverId}/removeFollower")
    suspend fun removeFollower(
        @PathVariable loverId: Long,
        @PathVariable targetLoverId: Long
    ): List<LoverViewWithoutRelationResponse> {
        return relationService.removeFollower(loverId, targetLoverId)
    }

}
