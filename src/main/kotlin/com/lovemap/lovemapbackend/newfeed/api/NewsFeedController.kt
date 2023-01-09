package com.lovemap.lovemapbackend.newfeed.api

import com.lovemap.lovemapbackend.newfeed.NewsFeedService
import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemResponse
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
    suspend fun getPage(@RequestParam("page") page: Int, @RequestParam("size") size: Int): List<NewsFeedItemResponse> {
        return cachedNewsFeedService.getNewsFeedPage(page, size)
    }
}
