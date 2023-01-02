package com.lovemap.lovemapbackend.lovespot.photo.like

import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface PhotoLikeRepository : CoroutineSortingRepository<PhotoLike, Long> {
    suspend fun getByPhotoIdAndLoverId(photoId: Long, loverId: Long): PhotoLike?
    fun findByPhotoId(photoId: Long): Flow<PhotoLike>
    suspend fun deleteByPhotoId(photoId: Long)
}

interface PhotoLikersDislikersRepository : CoroutineSortingRepository<PhotoLikersDislikers, Long> {
    suspend fun findByPhotoId(photoId: Long): PhotoLikersDislikers?
    suspend fun deleteByPhotoId(photoId: Long)
}
