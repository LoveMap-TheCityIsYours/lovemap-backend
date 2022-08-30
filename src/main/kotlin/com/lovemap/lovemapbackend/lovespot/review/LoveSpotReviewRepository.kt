package com.lovemap.lovemapbackend.lovespot.review

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface LoveSpotReviewRepository : CoroutineSortingRepository<LoveSpotReview, Long> {
    suspend fun findByReviewerIdAndLoveSpotId(reviewerId: Long, loveSpotId: Long): LoveSpotReview?
    suspend fun findByReviewerIdAndLoveId(reviewerId: Long, loveId: Long): LoveSpotReview?

    fun findAllByLoveSpotIdIn(spotIds: Collection<Long>): Flow<LoveSpotReview>

    fun findAllByReviewerId(reviewerId: Long): Flow<LoveSpotReview>

    suspend fun deleteByLoveSpotId(loveSpotId: Long)

    suspend fun deleteByReviewerIdAndLoveSpotId(reviewerId: Long, loveSpotId: Long): Boolean
}