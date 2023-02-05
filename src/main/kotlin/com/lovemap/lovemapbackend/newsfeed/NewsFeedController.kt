package com.lovemap.lovemapbackend.newsfeed

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

    @GetMapping("v2")
    suspend fun getV2Page(
        @RequestParam("page") page: Int,
        @RequestParam("size") size: Int
    ): List<NewsFeedItemResponse> {
        return cachedNewsFeedService.getProcessedNewsFeedPage(page, size)
    }
}
