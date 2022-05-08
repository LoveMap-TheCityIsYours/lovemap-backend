package com.lovemap.lovemapbackend.lover

import com.lovemap.lovemapbackend.relation.Relation
import com.lovemap.lovemapbackend.relation.RelationStatusDto
import com.lovemap.lovemapbackend.relation.LoverRelations
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.time.Instant
import javax.validation.constraints.Email
import javax.validation.constraints.Size

data class LoverDto(
    val id: Long,
    val userName: String,
    val email: String,
    val rank: Int,
    val points: Int,
    val numberOfLoves: Int,
    val numberOfReports: Int,
    val loveSpotsAdded: Int,
    val numberOfFollowers: Int,
    val createdAt: Instant,
    val shareableLink: String? = null,
) {
    companion object {
        fun of(lover: Lover): LoverDto {
            return LoverDto(
                id = lover.id,
                userName = lover.userName,
                email = lover.email,
                rank = lover.rank,
                points = lover.points,
                numberOfLoves = lover.numberOfLoves,
                numberOfReports = lover.numberOfReports,
                loveSpotsAdded = lover.loveSpotsAdded,
                numberOfFollowers = lover.numberOfFollowers,
                createdAt = lover.createdAt.toInstant(),
                shareableLink = lover.link,
            )
        }
    }
}

data class LoverRelationsDto(
    val id: Long,
    val relations: List<LoverViewDto>,
    val userName: String,
    val email: String,
    val rank: Int,
    val points: Int,
    val numberOfLoves: Int,
    val numberOfReports: Int,
    val loveSpotsAdded: Int,
    val numberOfFollowers: Int,
    val createdAt: Instant,
    val shareableLink: String? = null,
) {
    companion object {
        suspend fun of(lover: Lover, loverRelations: LoverRelations): LoverRelationsDto {
            return LoverRelationsDto(
                id = lover.id,
                userName = lover.userName,
                email = lover.email,
                rank = lover.rank,
                points = lover.points,
                numberOfLoves = lover.numberOfLoves,
                numberOfReports = lover.numberOfReports,
                loveSpotsAdded = lover.loveSpotsAdded,
                numberOfFollowers = lover.numberOfFollowers,
                createdAt = lover.createdAt.toInstant(),
                shareableLink = lover.link,
                relations = loverRelations.relations.map { entry ->
                    LoverViewDto(
                        entry.loverView.id,
                        entry.loverView.userName,
                        entry.rank,
                        RelationStatusDto.of(entry.relationStatus)
                    )
                }.toList()
            )
        }
    }
}

data class CreateLoverRequest(
    @field:Size(min = 3, max = 25, message = "Length of username must be between 3 and 25 characters.")
    val userName: String,
    @field:Size(min = 6, max = 100, message = "Length of password must be between 6 and 100 characters.")
    val password: String,
    @field:Email(message = "Invalid email address")
    val email: String
)

data class LoginLoverRequest(
    @field:Size(min = 3, max = 25, message = "Length of username must be between 3 and 25 characters.")
    val userName: String?,
    @field:Email(message = "Invalid email address")
    val email: String?,
    @field:Size(min = 6, max = 100, message = "Length of password must be between 6 and 100 characters.")
    val password: String
)

data class LoverViewDto(
    val id: Long,
    val userName: String,
    val rank: Int,
    val relation: RelationStatusDto
) {
    companion object {
        fun of(lover: Lover, relationStatus: Relation.Status): LoverViewDto {
            return LoverViewDto(
                id = lover.id,
                userName = lover.userName,
                rank = lover.rank,
                relation = RelationStatusDto.of(relationStatus)
            )
        }

        fun of(lover: Lover, relationStatus: RelationStatusDto): LoverViewDto {
            return LoverViewDto(
                id = lover.id,
                userName = lover.userName,
                rank = lover.rank,
                relation = relationStatus
            )
        }
    }
}