package com.lovemap.lovemapbackend.newsfeed.api

import com.lovemap.lovemapbackend.newsfeed.NewsFeedService
import com.lovemap.lovemapbackend.newsfeed.model.response.NewsFeedItemResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/newsfeed")
class NewsFeedController(
    private val cachedNewsFeedService: NewsFeedService
) {

    @GetMapping("/get-whole-feed")
    suspend fun getWholeFeed(): List<NewsFeedItemResponse> {
        return cachedNewsFeedService.getWholeFeed()
    }

    @GetMapping
    @Deprecated("Will be removed when new app version 74 is distributed well enough")
    suspend fun getPage(@RequestParam("page") page: Int, @RequestParam("size") size: Int): List<NewsFeedItemResponse> {
        return cachedNewsFeedService.getNewsFeedPage(page, size)
    }

    @GetMapping("v2")
    suspend fun getV2Page(@RequestParam("page") page: Int, @RequestParam("size") size: Int): List<NewsFeedItemResponse> {
        return cachedNewsFeedService.getProcessedNewsFeedPage(page, size)
    }
}
