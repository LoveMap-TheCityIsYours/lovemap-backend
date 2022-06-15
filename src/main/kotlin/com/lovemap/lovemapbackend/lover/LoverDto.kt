package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.love.LoveDto
import com.lovemap.lovemapbackend.lovespot.LoveSpotDto
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewDto
import com.lovemap.lovemapbackend.relation.LoverRelations
import com.lovemap.lovemapbackend.relation.Relation
import com.lovemap.lovemapbackend.relation.RelationStatusDto
import com.lovemap.lovemapbackend.utils.InstantConverterUtils.toApiString
import com.lovemap.lovemapbackend.utils.INVALID_EMAIL
import com.lovemap.lovemapbackend.utils.INVALID_PASSWORD
import com.lovemap.lovemapbackend.utils.INVALID_USERNAME
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import javax.validation.constraints.Email
import javax.validation.constraints.Size

data class LoverDto(
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
        fun of(lover: Lover, isAdmin: Boolean = false): LoverDto {
            return LoverDto(
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

data class LoverContributionsDto(
    val loves: List<LoveDto>,
    val loveSpots: List<LoveSpotDto>,
    val loveSpotReviews: List<LoveSpotReviewDto>
)

data class LoverRelationsDto(
    val id: Long,
    val relations: List<LoverViewDto>,
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
        suspend fun of(lover: Lover, loverRelations: LoverRelations, isAdmin: Boolean = false): LoverRelationsDto {
            return LoverRelationsDto(
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
                    LoverViewDto(
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

data class LoverViewDto(
    val id: Long,
    val userName: String,
    val points: Int,
    val rank: Int,
    val createdAt: String,
    val relation: RelationStatusDto,
    val publicProfile: Boolean,
) {
    companion object {
        fun of(lover: Lover, relationStatus: Relation.Status): LoverViewDto {
            return LoverViewDto(
                id = lover.id,
                userName = lover.userName,
                points = lover.points,
                rank = lover.rank,
                createdAt = lover.createdAt.toInstant().toApiString(),
                relation = RelationStatusDto.of(relationStatus),
                publicProfile = false
            )
        }

        fun of(lover: Lover, relationStatus: RelationStatusDto): LoverViewDto {
            return LoverViewDto(
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
