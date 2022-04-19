package com.smackmap.smackmapbackend.smack.location.review

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface SmackLocationReviewRepository : CoroutineSortingRepository<SmackLocationReview, Long> {
    suspend fun findByReviewerIdAndSmackLocationId(reviewerId: Long, smackLocationId: Long): SmackLocationReview?
    fun findAllBySmackLocationIdIn(locationIds: Collection<Long>): Flow<SmackLocationReview>
}