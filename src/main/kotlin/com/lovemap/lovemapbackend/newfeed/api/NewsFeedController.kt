package com.lovemap.lovemapbackend.newfeed.api

import com.lovemap.lovemapbackend.newfeed.CachedNewsFeedService
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/newsfeed")
class NewsFeedController(
    private val cachedNewsFeedService: CachedNewsFeedService
) {

    @GetMapping("/get-whole-feed")
    suspend fun getWholeFeed(): List<NewsFeedItemResponse> {
        return cachedNewsFeedService.getWholeFeed()
    }
}