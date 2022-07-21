package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.love.LoveResponse
import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewResponse
import com.lovemap.lovemapbackend.relation.LoverRelations
import com.lovemap.lovemapbackend.relation.Relation
import com.lovemap.lovemapbackend.relation.RelationStatusDto
import com.lovemap.lovemapbackend.utils.InstantConverterUtils.toApiString
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

data class LoverResponse(
    val id: Long,
    val userName: String,
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
) {
    companion object {
        fun of(lover: Lover, isAdmin: Boolean = false): LoverResponse {
            return LoverResponse(
                id = lover.id,
                userName = lover.userName,
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
                publicProfile = false,
                shareableLink = lover.uuid?.let { LoverService.linkPrefixVisible + lover.uuid },
                isAdmin = isAdmin
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
) {
    companion object {
        suspend fun of(lover: Lover, loverRelations: LoverRelations, isAdmin: Boolean = false): LoverRelationsResponse {
            return LoverRelationsResponse(
                id = lover.id,
                userName = lover.userName,
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
                publicProfile = false,
                shareableLink = lover.uuid?.let { LoverService.linkPrefixVisible + lover.uuid },
                isAdmin = isAdmin,
                relations = loverRelations.relations.map { entry ->
                    LoverViewResponse(
                        id = entry.loverView.id,
                        userName = entry.loverView.userName,
                        points = lover.points,
                        rank = entry.rank,
                        createdAt = entry.loverView.createdAt.toInstant().toApiString(),
                        relation = RelationStatusDto.of(entry.relationStatus),
                        publicProfile = false
                    )
                }.toList()
            )
        }
    }
}

data class LoverViewResponse(
    val id: Long,
    val userName: String,
    val points: Int,
    val rank: Int,
    val createdAt: String,
    val relation: RelationStatusDto,
    val publicProfile: Boolean,
) {
    companion object {
        fun of(lover: Lover, relationStatus: Relation.Status): LoverViewResponse {
            return LoverViewResponse(
                id = lover.id,
                userName = lover.userName,
                points = lover.points,
                rank = lover.rank,
                createdAt = lover.createdAt.toInstant().toApiString(),
                relation = RelationStatusDto.of(relationStatus),
                publicProfile = false
            )
        }

        fun of(lover: Lover, relationStatus: RelationStatusDto): LoverViewResponse {
            return LoverViewResponse(
                id = lover.id,
                userName = lover.userName,
                points = lover.points,
                rank = lover.rank,
                createdAt = lover.createdAt.toInstant().toApiString(),
                relation = relationStatus,
                publicProfile = false
            )
        }
    }
}
