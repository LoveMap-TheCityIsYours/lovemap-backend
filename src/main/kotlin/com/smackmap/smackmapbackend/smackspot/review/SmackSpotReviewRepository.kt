package com.smackmap.smackmapbackend.smackspot.review

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface SmackSpotReviewRepository : CoroutineSortingRepository<SmackSpotReview, Long> {
    suspend fun findByReviewerIdAndSmackSpotId(reviewerId: Long, smackSpotId: Long): SmackSpotReview?

    fun findAllBySmackSpotIdIn(spotIds: Collection<Long>): Flow<SmackSpotReview>

    fun findAllByReviewerId(reviewerId: Long): Flow<SmackSpotReview>
}