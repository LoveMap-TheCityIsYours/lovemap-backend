package com.lovemap.lovemapbackend.lovespot.review

import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.love.Love
import com.lovemap.lovemapbackend.love.LoveService
import com.lovemap.lovemapbackend.lover.LoverPointService
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotStatisticsService
import com.lovemap.lovemapbackend.newfeed.NewsFeedDeletionService
import com.lovemap.lovemapbackend.newfeed.data.NewsFeedItem
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.ErrorMessage
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant

@Service
@Transactional
class LoveSpotReviewService(
    private val authorizationService: AuthorizationService,
    private val loveService: LoveService,
    private val loveSpotStatService: LoveSpotStatisticsService,
    private val loverPointService: LoverPointService,
    private val repository: LoveSpotReviewRepository,
    private val newsFeedDeletionService: NewsFeedDeletionService
) {

    fun findAllByLoveSpotIdIn(loveSpotIds: List<Long>): Flow<LoveSpotReview> {
        return repository.findAllByLoveSpotIdIn(loveSpotIds)
    }

    fun findAllByReviewerId(reviewerId: Long): Flow<LoveSpotReview> {
        return repository.findAllByReviewerId(reviewerId)
    }

    suspend fun authorizedGetById(loveSpotId: Long, reviewId: Long): LoveSpotReview {
        val review = getById(reviewId)
        if (review.loveSpotId != loveSpotId) {
            throw LoveMapException(HttpStatus.BAD_REQUEST, ErrorCode.NotFoundById)
        }
        authorizationService.checkAccessFor(review.reviewerId)
        return review
    }

    suspend fun getById(reviewId: Long): LoveSpotReview {
        return repository.findById(reviewId)
            ?: throw LoveMapException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    ErrorCode.NotFoundById,
                    reviewId.toString(),
                    "LoveSpotReview not found by ID '$reviewId'."
                )
            )
    }

    suspend fun addOrUpdateReview(request: LoveSpotReviewRequest): LoveSpot {
        authorizationService.checkAccessFor(request.reviewerId)
        validateReview(request)
        val spotReview = repository
            .findByReviewerIdAndLoveSpotId(request.reviewerId, request.loveSpotId)
        if (spotReview != null) {
            return updateReview(spotReview, request)
        }
        return addReview(request)
    }

    private suspend fun updateReview(
        spotReview: LoveSpotReview,
        request: LoveSpotReviewRequest
    ): LoveSpot {
        spotReview.reviewText = request.reviewText.trim()
        spotReview.reviewStars = request.reviewStars
        spotReview.riskLevel = request.riskLevel
        repository.save(spotReview)
        val reviews = repository.findAllByLoveSpotIdIn(listOf(request.loveSpotId)).toList()
        val loveSpot = loveSpotStatService.recalculateLoveSpotReviews(request.loveSpotId, reviews)
        loverPointService.updatePointsForReviewUpdate(spotReview, request, loveSpot)
        return loveSpot
    }

    private suspend fun addReview(request: LoveSpotReviewRequest): LoveSpot {
        val review = repository.save(
            LoveSpotReview(
                loveId = request.loveId,
                reviewerId = request.reviewerId,
                submittedAt = Timestamp.from(Instant.now()),
                loveSpotId = request.loveSpotId,
                reviewStars = request.reviewStars,
                reviewText = request.reviewText.trim(),
                riskLevel = request.riskLevel,
            )
        )
        val reviews = repository.findAllByLoveSpotIdIn(listOf(request.loveSpotId)).toList()
        val loveSpot = loveSpotStatService.recalculateLoveSpotReviews(request.loveSpotId, reviews)
        loverPointService.addPointsForReview(review, loveSpot)
        return loveSpot
    }

    private suspend fun validateReview(request: LoveSpotReviewRequest) {
        checkLocationsMatch(request)
    }

    private suspend fun checkLocationsMatch(
        request: LoveSpotReviewRequest
    ) {
        val love: Love = loveService.getById(request.loveId)
        if (request.loveSpotId != love.loveSpotId) {
            throw LoveMapException(
                HttpStatus.BAD_REQUEST,
                ErrorMessage(
                    ErrorCode.BadRequest,
                    request.loveSpotId.toString(),
                    "Requested loveSpotId '${request.loveSpotId}' " +
                            "does not match with the love's spotId '${love.loveSpotId}'"
                )
            )
        }
    }

    suspend fun deleteReviewsByLove(love: Love) {
        deleteReviewByLoverAndLove(love.loveSpotId, love.loverId, love.id)
        deleteReviewByLoverAndLove(love.loveSpotId, love.loverPartnerId, love.id)
    }

    suspend fun getReviewsByLove(love: Love): List<LoveSpotReview> {
        val review1 = repository.findByReviewerIdAndLoveId(love.loverId, love.id)
        val review2 = love.loverPartnerId?.let { repository.findByReviewerIdAndLoveId(it, love.id) }
        return listOfNotNull(review1, review2)
    }

    suspend fun deleteReviewByLoverAndLove(loveSpotId: Long, loverId: Long?, loveId: Long) {
        loverId?.let {
            val review = repository.findByReviewerIdAndLoveId(loverId, loveId)
            if (review != null) {
                newsFeedDeletionService.deleteByTypeAndReferenceId(NewsFeedItem.Type.LOVE_SPOT_REVIEW, review.id)
                repository.delete(review)
                val reviews = repository.findAllByLoveSpotIdIn(listOf(loveSpotId)).toList()
                val loveSpot = loveSpotStatService.recalculateLoveSpotReviews(loveSpotId, reviews)
                loverPointService.subtractPointsForReviewDeleted(review, loveSpot)
            }
        }
    }

    suspend fun updatePhotoCounter(reviewId: Long, reviewPhotoCount: Int) {
        repository.findById(reviewId)?.let {
            it.numberOfPhotos = reviewPhotoCount
            repository.save(it)
        }
    }

    fun getPhotosFrom(generateFrom: Instant): Flow<LoveSpotReview> {
        return repository.findAllAfterSubmittedAt(Timestamp.from(generateFrom))
    }
}
