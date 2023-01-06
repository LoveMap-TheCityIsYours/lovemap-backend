package com.lovemap.lovemapbackend.lovespot.photo.like

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import java.sql.Timestamp

interface PhotoLikeRepository : CoroutineSortingRepository<PhotoLike, Long>, CoroutineCrudRepository<PhotoLike, Long> {
    suspend fun getByPhotoIdAndLoverId(photoId: Long, loverId: Long): PhotoLike?
    fun findByPhotoId(photoId: Long): Flow<PhotoLike>
    suspend fun deleteByPhotoId(photoId: Long)

    @Query(
        """
            SELECT * FROM photo_like
            WHERE happened_at > :happenedAt
            ORDER BY happened_at DESC
        """
    )
    fun findAllAfterHappenedAt(happenedAt: Timestamp): Flow<PhotoLike>

}

interface PhotoLikersDislikersRepository : CoroutineSortingRepository<PhotoLikersDislikers, Long>,
    CoroutineCrudRepository<PhotoLikersDislikers, Long> {
    suspend fun findByPhotoId(photoId: Long): PhotoLikersDislikers?
    suspend fun deleteByPhotoId(photoId: Long)
}
