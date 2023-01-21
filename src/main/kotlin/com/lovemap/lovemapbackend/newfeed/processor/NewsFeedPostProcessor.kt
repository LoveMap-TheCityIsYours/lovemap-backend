package com.lovemap.lovemapbackend.newfeed.processor

import com.lovemap.lovemapbackend.newfeed.model.NewsFeedItemDto

interface NewsFeedPostProcessor<T> {

    fun processNewsFeed(newsFeed: Collection<NewsFeedItemDto>, context: T): List<NewsFeedItemDto>
}