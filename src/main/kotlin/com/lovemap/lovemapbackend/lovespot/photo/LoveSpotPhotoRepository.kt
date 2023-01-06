package com.lovemap.lovemapbackend.lovespot.photo

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import java.sql.Timestamp

interface LoveSpotPhotoRepository : CoroutineSortingRepository<LoveSpotPhoto, Long>,
    CoroutineCrudRepository<LoveSpotPhoto, Long> {

    suspend fun countByLoveSpotReviewId(loveSpotId: Long): Long

    fun findAllByLoveSpotId(loveSpotId: Long): Flow<LoveSpotPhoto>
    fun findAllByLoveSpotIdOrderByLikesDesc(loveSpotId: Long): Flow<LoveSpotPhoto>

    fun findAllByLoveSpotIdAndLoveSpotReviewId(loveSpotId: Long, reviewId: Long): Flow<LoveSpotPhoto>
    fun findAllByLoveSpotIdAndLoveSpotReviewIdOrderByLikesDesc(loveSpotId: Long, reviewId: Long): Flow<LoveSpotPhoto>

    @Query(
        """
            SELECT * FROM love_location_photo
            WHERE uploaded_at > :uploadedAt
            ORDER BY uploaded_at DESC
        """
    )
    fun findAllAfterUploadedAt(uploadedAt: Timestamp): Flow<LoveSpotPhoto>
}