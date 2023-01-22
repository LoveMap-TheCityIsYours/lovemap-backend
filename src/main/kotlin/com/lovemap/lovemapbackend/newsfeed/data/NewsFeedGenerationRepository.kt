package com.lovemap.lovemapbackend.newsfeed.data

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface NewsFeedGenerationRepository : CoroutineSortingRepository<NewsFeedGeneration, Long>,
    CoroutineCrudRepository<NewsFeedGeneration, Long> {

    @Query(
        """
        SELECT * FROM news_feed_generation 
        ORDER BY generated_at DESC 
        LIMIT 1
        """
    )
    suspend fun getLastGeneration(): NewsFeedGeneration?

}