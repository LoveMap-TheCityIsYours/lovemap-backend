package com.smackmap.smackmapbackend.smacker

import com.smackmap.smackmapbackend.relation.Relation
import com.smackmap.smackmapbackend.relation.RelationStatusDto
import com.smackmap.smackmapbackend.relation.SmackerRelations
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.data.relational.core.mapping.Column
import java.sql.Timestamp
import java.time.Instant
import javax.validation.constraints.Email
import javax.validation.constraints.Size

data class SmackerDto(
    val id: Long,
    val userName: String,
    val email: String,
    val rank: Int,
    val points: Int,
    val numberOfSmacks: Int,
    val numberOfReports: Int,
    val smackSpotsAdded: Int,
    val numberOfFollowers: Int,
    val createdAt: Instant,
    val shareableLink: String? = null,
) {
    companion object {
        fun of(smacker: Smacker): SmackerDto {
            return SmackerDto(
                id = smacker.id,
                userName = smacker.userName,
                email = smacker.email,
                rank = smacker.rank,
                points = smacker.points,
                numberOfSmacks = smacker.numberOfSmacks,
                numberOfReports = smacker.numberOfReports,
                smackSpotsAdded = smacker.smackSpotsAdded,
                numberOfFollowers = smacker.numberOfFollowers,
                createdAt = smacker.createdAt.toInstant(),
                shareableLink = smacker.link,
            )
        }
    }
}

data class SmackerRelationsDto(
    val id: Long,
    val relations: List<SmackerViewDto>,
    val userName: String,
    val email: String,
    val rank: Int,
    val points: Int,
    val numberOfSmacks: Int,
    val numberOfReports: Int,
    val smackSpotsAdded: Int,
    val numberOfFollowers: Int,
    val createdAt: Instant,
    val shareableLink: String? = null,
) {
    companion object {
        suspend fun of(smacker: Smacker, smackerRelations: SmackerRelations): SmackerRelationsDto {
            return SmackerRelationsDto(
                id = smacker.id,
                userName = smacker.userName,
                email = smacker.email,
                rank = smacker.rank,
                points = smacker.points,
                numberOfSmacks = smacker.numberOfSmacks,
                numberOfReports = smacker.numberOfReports,
                smackSpotsAdded = smacker.smackSpotsAdded,
                numberOfFollowers = smacker.numberOfFollowers,
                createdAt = smacker.createdAt.toInstant(),
                shareableLink = smacker.link,
                relations = smackerRelations.relations.map { entry ->
                    SmackerViewDto(
                        entry.smackerView.id,
                        entry.smackerView.userName,
                        entry.rank,
                        RelationStatusDto.of(entry.relationStatus)
                    )
                }.toList()
            )
        }
    }
}

data class CreateSmackerRequest(
    @field:Size(min = 3, max = 25, message = "Length of username must be between 3 and 25 characters.")
    val userName: String,
    @field:Size(min = 6, max = 100, message = "Length of password must be between 6 and 100 characters.")
    val password: String,
    @field:Email(message = "Invalid email address")
    val email: String
)

data class LoginSmackerRequest(
    @field:Size(min = 3, max = 25, message = "Length of username must be between 3 and 25 characters.")
    val userName: String?,
    @field:Email(message = "Invalid email address")
    val email: String?,
    @field:Size(min = 6, max = 100, message = "Length of password must be between 6 and 100 characters.")
    val password: String
)

data class SmackerViewDto(
    val id: Long,
    val userName: String,
    val rank: Int,
    val relation: RelationStatusDto
) {
    companion object {
        fun of(smacker: Smacker, relationStatus: Relation.Status): SmackerViewDto {
            return SmackerViewDto(
                id = smacker.id,
                userName = smacker.userName,
                rank = smacker.rank,
                relation = RelationStatusDto.of(relationStatus)
            )
        }

        fun of(smacker: Smacker, relationStatus: RelationStatusDto): SmackerViewDto {
            return SmackerViewDto(
                id = smacker.id,
                userName = smacker.userName,
                rank = smacker.rank,
                relation = relationStatus
            )
        }
    }
}
