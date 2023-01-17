package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.love.LoveResponse
import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewResponse
import com.lovemap.lovemapbackend.relation.LoverRelations
import com.lovemap.lovemapbackend.relation.Relation
import com.lovemap.lovemapbackend.relation.RelationStatusDto
import com.lovemap.lovemapbackend.utils.INVALID_USERNAME
import com.lovemap.lovemapbackend.utils.InstantConverterUtils.toApiString
import jakarta.validation.constraints.Size
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

data class LoverResponse(
    val id: Long,
    val userName: String,
    val displayName: String,
    val email: String,
    val rank: Int,
    val points: Int,
    val numberOfLoves: Int,
    val reviewsSubmitted: Int,
    val reportsSubmitted: Int,
    val reportsReceived: Int,
    val loveSpotsAdded: Int,
    val numberOfFollowers: Int,
    val createdAt: String,
    val publicProfile: Boolean,
    val shareableLink: String? = null,
    val isAdmin: Boolean = false,
    val partnerId: Long?
) {
    companion object {
        fun of(lover: Lover, isAdmin: Boolean = false): LoverResponse {
            return LoverResponse(
                id = lover.id,
                userName = lover.userName,
                displayName = lover.displayName,
                email = lover.email,
                rank = lover.rank,
                points = lover.points,
                numberOfLoves = lover.numberOfLoves,
                reviewsSubmitted = lover.reviewsSubmitted,
                reportsSubmitted = lover.reportsSubmitted,
                reportsReceived = lover.reportsReceived,
                loveSpotsAdded = lover.loveSpotsAdded,
                numberOfFollowers = lover.numberOfFollowers,
                createdAt = lover.createdAt.toInstant().toApiString(),
                publicProfile = lover.publicProfile,
                shareableLink = lover.uuid?.let { LoverService.linkPrefixVisible + lover.uuid },
                isAdmin = isAdmin,
                partnerId = lover.partnerId
            )
        }
    }
}

data class LoverContributionsResponse(
    val loves: List<LoveResponse>,
    val loveSpots: List<LoveSpotResponse>,
    val loveSpotReviews: List<LoveSpotReviewResponse>
)

data class LoverRelationsResponse(
    val id: Long,
    val relations: List<LoverViewResponse>,
    val userName: String,
    val displayName: String,
    val email: String,
    val rank: Int,
    val points: Int,
    val numberOfLoves: Int,
    val reviewsSubmitted: Int,
    val reportsSubmitted: Int,
    val reportsReceived: Int,
    val loveSpotsAdded: Int,
    val numberOfFollowers: Int,
    val createdAt: String,
    val publicProfile: Boolean,
    val shareableLink: String? = null,
    val isAdmin: Boolean = false,
    val partnerId: Long?
) {
    companion object {
        suspend fun of(lover: Lover, loverRelations: LoverRelations, isAdmin: Boolean = false): LoverRelationsResponse {
            return LoverRelationsResponse(
                id = lover.id,
                userName = lover.userName,
                displayName = lover.displayName,
                email = lover.email,
                rank = lover.rank,
                points = lover.points,
                numberOfLoves = lover.numberOfLoves,
                reviewsSubmitted = lover.reviewsSubmitted,
                reportsSubmitted = lover.reportsSubmitted,
                reportsReceived = lover.reportsReceived,
                loveSpotsAdded = lover.loveSpotsAdded,
                numberOfFollowers = lover.numberOfFollowers,
                createdAt = lover.createdAt.toInstant().toApiString(),
                publicProfile = lover.publicProfile,
                shareableLink = lover.uuid?.let { LoverService.linkPrefixVisible + lover.uuid },
                isAdmin = isAdmin,
                partnerId = lover.partnerId,
                relations = loverRelations.relations.map { entry ->
                    LoverViewResponse(
                        id = entry.loverView.id,
                        userName = lover.displayName,
                        displayName = entry.loverView.displayName,
                        points = lover.points,
                        rank = entry.rank,
                        createdAt = entry.loverView.createdAt.toInstant().toApiString(),
                        relation = RelationStatusDto.of(entry.relationStatus),
                        publicProfile = false,
                        partnerId = entry.loverView.partnerId
                    )
                }.toList()
            )
        }
    }
}

data class LoverViewWithoutRelationResponse(
    val id: Long,
    val displayName: String,
    val points: Int,
    val rank: Int,
    val createdAt: String,
    val publicProfile: Boolean,
    val partnerId: Long?,
) {
    companion object {
        fun of(lover: Lover): LoverViewWithoutRelationResponse {
            return LoverViewWithoutRelationResponse(
                id = lover.id,
                displayName = lover.displayName,
                points = lover.points,
                rank = lover.rank,
                createdAt = lover.createdAt.toInstant().toApiString(),
                publicProfile = lover.publicProfile,
                partnerId = lover.partnerId
            )
        }
    }
}

data class LoverViewResponse(
    val id: Long,
    val userName: String, // keeping for backward compatibility
    val displayName: String,
    val points: Int,
    val rank: Int,
    val createdAt: String,
    val relation: RelationStatusDto,
    val publicProfile: Boolean,
    val partnerId: Long?,
) {
    companion object {
        fun of(lover: Lover, relationStatus: Relation.Status): LoverViewResponse {
            return LoverViewResponse(
                id = lover.id,
                userName = lover.displayName,
                displayName = lover.displayName,
                points = lover.points,
                rank = lover.rank,
                createdAt = lover.createdAt.toInstant().toApiString(),
                relation = RelationStatusDto.of(relationStatus),
                publicProfile = lover.publicProfile,
                partnerId = lover.partnerId
            )
        }

        fun of(lover: Lover, relationStatus: RelationStatusDto): LoverViewResponse {
            return LoverViewResponse(
                id = lover.id,
                userName = lover.displayName,
                displayName = lover.displayName,
                points = lover.points,
                rank = lover.rank,
                createdAt = lover.createdAt.toInstant().toApiString(),
                relation = relationStatus,
                publicProfile = lover.publicProfile,
                partnerId = lover.partnerId
            )
        }
    }
}

data class UpdateLoverRequest(
    val email: String?,

    @field:Size(min = 3, max = 32, message = INVALID_USERNAME)
    val displayName: String?,

    val publicProfile: Boolean? = null
)
