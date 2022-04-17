package com.smackmap.smackmapbackend.smacker

import com.smackmap.smackmapbackend.relation.Relation
import com.smackmap.smackmapbackend.relation.RelationStatusDto
import com.smackmap.smackmapbackend.relation.SmackerRelations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

data class SmackerDto(
    val id: Long,
    val userName: String,
    val email: String,
    val shareableLink: String? = null
) {
    companion object {
        fun of(smacker: Smacker): SmackerDto {
            return SmackerDto(
                id = smacker.id,
                userName = smacker.userName,
                email = smacker.email,
                shareableLink = smacker.link,
            )
        }
    }
}

data class SmackerRelationsDto(
    val id: Long,
    val relations: Flow<SmackerViewDto>,
    val userName: String,
    val email: String,
    val shareableLink: String? = null
) {
    companion object {
        fun of(smacker: Smacker, smackerRelations: SmackerRelations): SmackerRelationsDto {
            return SmackerRelationsDto(
                id = smacker.id,
                userName = smacker.userName,
                email = smacker.email,
                shareableLink = smacker.link,
                relations = smackerRelations.relations.map { entry ->
                    SmackerViewDto(
                        entry.smackerView.id,
                        entry.smackerView.userName,
                        RelationStatusDto.of(entry.relationStatus)
                    )
                }
            )
        }
    }
}

data class CreateSmackerRequest(
    val userName: String,
    val password: String,
    val email: String
)

data class LoginSmackerRequest(
    val userName: String?,
    val email: String?,
    val password: String
)

data class SmackerViewDto(
    val id: Long,
    val userName: String,
    val relation: RelationStatusDto
) {
    companion object {
        fun of(smacker: Smacker, relationStatus: Relation.Status): SmackerViewDto {
            return SmackerViewDto(
                id = smacker.id,
                userName = smacker.userName,
                relation = RelationStatusDto.of(relationStatus)
            )
        }

        fun of(smacker: Smacker, relationStatus: RelationStatusDto): SmackerViewDto {
            return SmackerViewDto(
                id = smacker.id,
                userName = smacker.userName,
                relation = relationStatus
            )
        }
    }
}

data class GenerateSmackerLinkRequest(
    val smackerId: Long
)

data class SmackerLinkDto(
    val smackerId: Long,
    val shareableLink: String
)
