package com.smackmap.smackmapbackend.smack.location.review

data class SmackLocationReviewDto(
    val id: Long,
    val smackId: Long,
    val reviewerId: Long,
    val smackLocationId: Long,
    val reviewText: String,
    val reviewStars: Int,
) {
    companion object {
        fun of(review: SmackLocationReview): SmackLocationReviewDto {
            return SmackLocationReviewDto(
                id = review.id,
                smackId = review.smackId,
                reviewerId = review.reviewerId,
                smackLocationId = review.smackLocationId,
                reviewText = review.reviewText,
                reviewStars = review.reviewStars
            )
        }
    }
}

data class SmackLocationReviewRequest(
    val smackId: Long,
    val reviewerId: Long,
    val smackLocationId: Long,
    val reviewText: String,
    val reviewStars: Int,
)
