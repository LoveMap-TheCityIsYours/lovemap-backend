package com.lovemap.lovemapbackend.lovespot.photo

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface LoveSpotPhotoRepository : CoroutineSortingRepository<LoveSpotPhoto, Long> {

    suspend fun countByLoveSpotId(loveSpotId: Long): Long
    suspend fun countByLoveSpotReviewId(loveSpotId: Long): Long

    fun findAllByLoveSpotId(loveSpotId: Long): Flow<LoveSpotPhoto>
    fun findAllByLoveSpotIdAndLoveSpotReviewId(loveSpotId: Long, reviewId: Long): Flow<LoveSpotPhoto>

}