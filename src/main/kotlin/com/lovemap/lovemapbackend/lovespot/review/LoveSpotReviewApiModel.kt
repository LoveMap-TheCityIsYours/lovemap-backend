package com.lovemap.lovemapbackend.lovespot.review

data class LoveSpotReviewResponse(
    val id: Long,
    val loveId: Long,
    val reviewerId: Long,
    val loveSpotId: Long,
    val reviewText: String,
    val reviewStars: Int,
    val riskLevel: Int,
) {
    companion object {
        fun of(review: LoveSpotReview): LoveSpotReviewResponse {
            return LoveSpotReviewResponse(
                id = review.id,
                loveId = review.loveId,
                reviewerId = review.reviewerId,
                loveSpotId = review.loveSpotId,
                reviewText = review.reviewText,
                reviewStars = review.reviewStars,
                riskLevel = review.riskLevel
            )
        }
    }
}

data class LoveSpotReviewRequest(
    val loveId: Long,
    val reviewerId: Long,
    val loveSpotId: Long,
    val reviewText: String,
    val reviewStars: Int,
    val riskLevel: Int,
)
