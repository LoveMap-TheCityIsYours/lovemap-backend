package com.lovemap.lovemapbackend.newfeed

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface NewsFeedRepository : CoroutineSortingRepository<NewsFeedItem, Long>,
    CoroutineCrudRepository<NewsFeedItem, Long> {
}