package com.lovemap.lovemapbackend.newsfeed.data

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import java.sql.Timestamp

interface NewsFeedRepository : CoroutineSortingRepository<NewsFeedItem, Long>,
    CoroutineCrudRepository<NewsFeedItem, Long> {

    suspend fun deleteByTypeAndReferenceId(type: NewsFeedItem.Type, referenceId: Long)

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

    @Query(
        """
            SELECT * FROM news_feed_item 
            WHERE lover_id = :loverId 
            ORDER BY happened_at DESC 
            LIMIT :limit
        """
    )
    fun findLastLimitOfLover(limit: Int, loverId: Long): Flow<NewsFeedItem>

    @Query(
        """
            SELECT * FROM news_feed_item 
            WHERE lover_id IN (:loverIds)  
            ORDER BY happened_at DESC 
            LIMIT :limit
        """
    )
    fun findLastLimitOfLoverIdsIn(limit: Int, loverIds: Set<Long>): Flow<NewsFeedItem>

    @Query(
        """
            SELECT * FROM news_feed_item
            WHERE type <> 'LOVER'
            ORDER BY happened_at DESC
            LIMIT :limit
        """
    )
    fun findLastLimitNotLovers(limit: Int): Flow<NewsFeedItem>

    @Query(
        """
            SELECT * FROM news_feed_item
            WHERE type = 'LOVER'
            ORDER BY happened_at DESC
            LIMIT :limit
        """
    )
    fun findLastLimitOnlyLovers(limit: Int): Flow<NewsFeedItem>

    fun findAllByType(type: NewsFeedItem.Type): Flow<NewsFeedItem>

    suspend fun findByTypeAndReferenceId(type: NewsFeedItem.Type, referenceId: Long): NewsFeedItem?
}