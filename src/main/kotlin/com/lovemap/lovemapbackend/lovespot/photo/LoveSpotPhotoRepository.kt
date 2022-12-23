package com.lovemap.lovemapbackend.lovespot.photo

import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface LoveSpotPhotoRepository : CoroutineSortingRepository<LoveSpotPhoto, Long> {

    suspend fun countByLoveSpotId(loveSpotId: Long): Long

    suspend fun countByLoveSpotReviewId(loveSpotId: Long): Long

}