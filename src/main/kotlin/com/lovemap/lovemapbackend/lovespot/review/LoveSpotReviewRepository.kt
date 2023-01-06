package com.lovemap.lovemapbackend.lovespot.review

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import java.sql.Timestamp

interface LoveSpotReviewRepository : CoroutineSortingRepository<LoveSpotReview, Long>,
    CoroutineCrudRepository<LoveSpotReview, Long> {
    suspend fun findByReviewerIdAndLoveSpotId(reviewerId: Long, loveSpotId: Long): LoveSpotReview?
    suspend fun findByReviewerIdAndLoveId(reviewerId: Long, loveId: Long): LoveSpotReview?

    fun findAllByLoveSpotIdIn(spotIds: Collection<Long>): Flow<LoveSpotReview>

    fun findAllByReviewerId(reviewerId: Long): Flow<LoveSpotReview>

    suspend fun deleteByLoveSpotId(loveSpotId: Long)

    suspend fun deleteByReviewerIdAndLoveSpotId(reviewerId: Long, loveSpotId: Long): Boolean

    @Query(
        """
            SELECT * FROM love_location_review
            WHERE submitted_at > :submittedAt
            ORDER BY submitted_at DESC
        """
    )
    fun findAllAfterSubmittedAt(submittedAt: Timestamp): Flow<LoveSpotReview>
}