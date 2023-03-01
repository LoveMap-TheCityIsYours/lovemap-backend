package com.lovemap.lovemapbackend.newsfeed.processor

interface NewsFeedPostProcessor<T> {

    suspend fun processNewsFeed(
        newsFeed: Collection<ProcessedNewsFeedItemDto>,
        context: T
    ): List<ProcessedNewsFeedItemDto>
}