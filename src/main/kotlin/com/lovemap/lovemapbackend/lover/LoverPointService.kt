package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.love.Love
import com.lovemap.lovemapbackend.lover.points.LoverPoints
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.photo.LoveSpotPhoto
import com.lovemap.lovemapbackend.lovespot.report.LoveSpotReport
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReview
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewRequest
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
class LoverPointService(
    private val loverService: LoverService,
    private val points: LoverPoints,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun addPointsForReview(review: LoveSpotReview, loveSpot: LoveSpot): Lover {
        val lover = addPointsForReviewSubmitted(review)
        addPointsForReviewReceived(review, loveSpot)
        return lover
    }

    private suspend fun addPointsForReviewSubmitted(review: LoveSpotReview): Lover {
        val lover = loverService.unAuthorizedGetById(review.reviewerId)
        if (isReviewMeaningful(review.reviewText)) {
            lover.points += points.reviewSubmitted
            lover.reviewsSubmitted += 1
        }
        return loverService.save(lover)
    }

    private suspend fun addPointsForReviewReceived(review: LoveSpotReview, loveSpot: LoveSpot) {
        if (review.reviewStars >= 4) {
            val spotAdder = loverService.unAuthorizedGetById(loveSpot.addedBy)
            spotAdder.points += if (review.reviewStars == 4) {
                points.reviewReceived4Stars
            } else {
                points.reviewReceived5Stars
            }
            loverService.save(spotAdder)
        }
    }

    suspend fun updatePointsForReviewUpdate(
        prevReview: LoveSpotReview,
        newRequest: LoveSpotReviewRequest,
        loveSpot: LoveSpot
    ): Lover {
        val lover = loverService.unAuthorizedGetById(prevReview.reviewerId)
        updatePointsForReviewSubmitted(prevReview, newRequest, lover)
        updatePointsForReviewReceived(loveSpot, prevReview, newRequest)
        return lover
    }

    private suspend fun updatePointsForReviewSubmitted(
        prevReview: LoveSpotReview,
        newRequest: LoveSpotReviewRequest,
        lover: Lover
    ) {
        if (isReviewMeaningful(prevReview.reviewText)) {
            if (isReviewMeaningful(newRequest.reviewText)) {
                // do nothing
            } else {
                // subtract points from lover
                lover.points -= points.reviewSubmitted
                lover.reviewsSubmitted -= 1
            }
        } else {
            if (isReviewMeaningful(newRequest.reviewText)) {
                // add points to lover
                lover.points += points.reviewSubmitted
                lover.reviewsSubmitted += 1
            } else {
                // do nothing
            }
        }
        loverService.save(lover)
    }

    private suspend fun updatePointsForReviewReceived(
        loveSpot: LoveSpot,
        prevReview: LoveSpotReview,
        newRequest: LoveSpotReviewRequest
    ) {
        val spotAdder = loverService.unAuthorizedGetById(loveSpot.addedBy)
        if (prevReview.reviewStars == 4) {
            if (newRequest.reviewStars == 4) {
                // do nothing
            } else if (newRequest.reviewStars >= 5) {
                spotAdder.points = spotAdder.points - points.reviewReceived4Stars + points.reviewReceived5Stars
            }
        } else if (prevReview.reviewStars >= 5) {
            if (newRequest.reviewStars == 4) {
                spotAdder.points = spotAdder.points - points.reviewReceived5Stars + points.reviewReceived4Stars
            } else if (newRequest.reviewStars >= 5) {
                // do nothing
            }
        }
        loverService.save(spotAdder)
    }

    suspend fun subtractPointsForReviewDeleted(deletedReview: LoveSpotReview, loveSpot: LoveSpot) {
        val lover = loverService.unAuthorizedGetById(deletedReview.reviewerId)
        subtractPointsForSubmittedReviewDeleted(deletedReview, lover)
        subtractPointsForReceivedReviewDeleted(loveSpot, deletedReview)
    }

    private suspend fun subtractPointsForSubmittedReviewDeleted(deletedReview: LoveSpotReview, lover: Lover) {
        if (isReviewMeaningful(deletedReview.reviewText)) {
            lover.points -= points.reviewSubmitted
            lover.reviewsSubmitted -= 1
        }
        loverService.save(lover)
    }

    private suspend fun subtractPointsForReceivedReviewDeleted(loveSpot: LoveSpot, deletedReview: LoveSpotReview) {
        if (deletedReview.reviewStars >= 4) {
            val spotAdder = loverService.unAuthorizedGetById(loveSpot.addedBy)
            spotAdder.points -= if (deletedReview.reviewStars == 4) {
                points.reviewReceived4Stars
            } else {
                points.reviewReceived5Stars
            }
            loverService.save(spotAdder)
        }
    }

    suspend fun addPointsForReport(report: LoveSpotReport, loveSpot: LoveSpot): Lover {
        val lover = addPointsForReportSubmitted(report)
        addPointsForReportReceived(loveSpot)
        return lover
    }

    private suspend fun addPointsForReportSubmitted(report: LoveSpotReport): Lover {
        val lover = loverService.unAuthorizedGetById(report.loverId)
        lover.points += points.reportSubmitted
        lover.reportsSubmitted += 1
        return loverService.save(lover)
    }

    private suspend fun addPointsForReportReceived(loveSpot: LoveSpot) {
        val spotAdder = loverService.unAuthorizedGetById(loveSpot.addedBy)
        spotAdder.points += points.reportReceived
        spotAdder.reportsReceived += 1
        loverService.save(spotAdder)
    }

    suspend fun addPointsForLovemaking(love: Love): Lover {
        val lover = loverService.unAuthorizedGetById(love.loverId)
        lover.points += points.loveMade
        lover.numberOfLoves += 1
        loverService.save(lover)
        love.loverPartnerId?.let {
            val partner = loverService.unAuthorizedGetById(it)
            partner.points += points.loveMade
            partner.numberOfLoves += 1
            loverService.save(partner)
        }
        return lover
    }

    suspend fun subtractPointsForLovemakingDeleted(love: Love) {
        val lover = loverService.unAuthorizedGetById(love.loverId)
        lover.points -= points.loveMade
        lover.numberOfLoves -= 1
        loverService.save(lover)
        love.loverPartnerId?.let {
            val partner = loverService.unAuthorizedGetById(it)
            partner.points -= points.loveMade
            partner.numberOfLoves -= 1
            loverService.save(partner)
        }
    }

    suspend fun addPointsForSpotAdded(loveSpot: LoveSpot): Lover {
        val lover = loverService.unAuthorizedGetById(loveSpot.addedBy)
        lover.points += points.loveSpotAdded
        lover.loveSpotsAdded += 1
        return loverService.save(lover)
    }

    suspend fun subtractPointsForSpotDeleted(loveSpot: LoveSpot): Lover {
        val lover = loverService.unAuthorizedGetById(loveSpot.addedBy)
        lover.points -= points.loveSpotAdded
        lover.loveSpotsAdded -= 1
        return loverService.save(lover)
    }

    private fun isReviewMeaningful(reviewText: String) =
        reviewText.length > 10

    suspend fun addPointsForPhotosAdded(loverId: Long, photoCount: Int): Lover {
        val lover = loverService.unAuthorizedGetById(loverId)
        lover.points += (points.photoUploaded * photoCount)
        lover.photosUploaded += photoCount
        return loverService.save(lover)
    }

    suspend fun subtractPointsForPhotoDeleted(photo: LoveSpotPhoto): Lover {
        val lover = loverService.unAuthorizedGetById(photo.uploadedBy)
        lover.points -= points.photoUploaded
        lover.photosUploaded -= 1
        // because I'm lazy to handle self-likes here, I rather keep the stats for deleted photos too
//        lover.points -= (photo.likes * points.photoLikeReceived)
//        lover.points -= (photo.dislikes * points.photoDislikeReceived)
//        lover.photoLikesReceived -= photo.likes
//        lover.photoDislikesReceived -= photo.dislikes
        return loverService.save(lover)
    }

    suspend fun addPointsForLike(photo: LoveSpotPhoto, lover: Lover) {
        if (photo.uploadedBy != lover.id) {
            val uploadedBy = loverService.unAuthorizedGetById(photo.uploadedBy)
            uploadedBy.points += points.photoLikeReceived
            uploadedBy.photoLikesReceived += 1
            loverService.save(uploadedBy)
        }
    }

    suspend fun subtractPointsForDislike(photo: LoveSpotPhoto, lover: Lover) {
        if (photo.uploadedBy != lover.id) {
            val uploadedBy = loverService.unAuthorizedGetById(photo.uploadedBy)
            uploadedBy.points += points.photoDislikeReceived
            uploadedBy.photoDislikesReceived += 1
            loverService.save(uploadedBy)
        }
    }

    suspend fun subtractPointsForLikeChangeToDislike(photo: LoveSpotPhoto, lover: Lover) {
        if (photo.uploadedBy != lover.id) {
            val uploadedBy = loverService.unAuthorizedGetById(photo.uploadedBy)
            uploadedBy.points -= points.photoLikeReceived
            uploadedBy.photoLikesReceived -= 1
            uploadedBy.points += points.photoDislikeReceived
            uploadedBy.photoDislikesReceived += 1
            loverService.save(uploadedBy)
        }
    }

    suspend fun addPointsForDislikeChangeToLike(photo: LoveSpotPhoto, lover: Lover) {
        if (photo.uploadedBy != lover.id) {
            val uploadedBy = loverService.unAuthorizedGetById(photo.uploadedBy)
            uploadedBy.points -= points.photoDislikeReceived
            uploadedBy.photoDislikesReceived -= 1
            uploadedBy.points += points.photoLikeReceived
            uploadedBy.photoLikesReceived += 1
            loverService.save(uploadedBy)
        }
    }

    suspend fun subtractPointsForUnlike(photo: LoveSpotPhoto, lover: Lover) {
        if (photo.uploadedBy != lover.id) {
            val uploadedBy = loverService.unAuthorizedGetById(photo.uploadedBy)
            uploadedBy.points -= points.photoLikeReceived
            uploadedBy.photoLikesReceived -= 1
            loverService.save(uploadedBy)
        }
    }

    suspend fun addPointsForUndislike(photo: LoveSpotPhoto, lover: Lover) {
        if (photo.uploadedBy != lover.id) {
            val uploadedBy = loverService.unAuthorizedGetById(photo.uploadedBy)
            uploadedBy.points -= points.photoDislikeReceived
            uploadedBy.photoDislikesReceived -= 1
            loverService.save(uploadedBy)
        }
    }
}