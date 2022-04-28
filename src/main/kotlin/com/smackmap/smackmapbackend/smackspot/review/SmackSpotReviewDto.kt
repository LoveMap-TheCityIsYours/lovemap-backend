package com.smackmap.smackmapbackend.smackspot.review

data class SmackSpotReviewDto(
    val id: Long,
    val smackId: Long,
    val reviewerId: Long,
    val smackSpotId: Long,
    val reviewText: String,
    val reviewStars: Int,
) {
    companion object {
        fun of(review: SmackSpotReview): SmackSpotReviewDto {
            return SmackSpotReviewDto(
                id = review.id,
                smackId = review.smackId,
                reviewerId = review.reviewerId,
                smackSpotId = review.smackSpotId,
                reviewText = review.reviewText,
                reviewStars = review.reviewStars
            )
        }
    }
}

data class SmackSpotReviewRequest(
    val smackId: Long,
    val reviewerId: Long,
    val smackSpotId: Long,
    val reviewText: String,
    val reviewStars: Int,
)
