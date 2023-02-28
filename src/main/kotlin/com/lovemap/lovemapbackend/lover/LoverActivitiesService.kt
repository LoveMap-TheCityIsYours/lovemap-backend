package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.newsfeed.NewsFeedService
import com.lovemap.lovemapbackend.newsfeed.api.NewsFeedItemResponse
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class LoverActivitiesService(
    private val loverService: LoverService,
    private val authorizationService: AuthorizationService,
    private val newsFeedService: NewsFeedService,
) {

    suspend fun getActivities(loverId: Long): List<NewsFeedItemResponse> {
        val lover = loverService.unAuthorizedGetById(loverId)
        return if (lover.publicProfile) {
            newsFeedService.getActivitiesOfLover(loverId)
        } else {
            val caller = authorizationService.getCaller()
            if (caller.partnerId == loverId) {
                newsFeedService.getActivitiesOfLover(loverId)
            } else {
                runCatching {
                    authorizationService.checkAccessFor(loverId)
                    newsFeedService.getActivitiesOfLover(loverId)
                }.getOrElse { throw LoveMapException(HttpStatus.BAD_REQUEST, ErrorCode.LoverIsNotPublic) }
            }
        }
    }
}