package com.lovemap.lovemapbackend.newsfeed.processor

import com.lovemap.lovemapbackend.newsfeed.model.NewsFeedItemDto

interface NewsFeedPostProcessor<T> {

    fun processNewsFeed(newsFeed: Collection<NewsFeedItemDto>, context: T): List<NewsFeedItemDto>
}