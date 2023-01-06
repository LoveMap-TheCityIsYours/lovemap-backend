package com.lovemap.lovemapbackend.newfeed

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import java.sql.Timestamp

interface NewsFeedRepository : CoroutineSortingRepository<NewsFeedItem, Long>,
    CoroutineCrudRepository<NewsFeedItem, Long> {

    @Query(
        """
            SELECT * FROM news_feed_item 
            ORDER BY id DESC  
            LIMIT 1
        """
    )
    suspend fun findLast(): NewsFeedItem?

    @Query(
        """
            SELECT * FROM news_feed_item
            WHERE generated_at > :generatedAt
            ORDER BY happened_at DESC
            LIMIT 200
        """
    )
    fun findAllAfterGeneratedAt(generatedAt: Timestamp): Flow<NewsFeedItem>

    @Query(
        """
            SELECT * FROM news_feed_item
            ORDER BY happened_at DESC
            LIMIT :limit
        """
    )
    fun findLastLimit(limit: Int): Flow<NewsFeedItem>
}