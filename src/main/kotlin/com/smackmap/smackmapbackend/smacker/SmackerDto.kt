package com.smackmap.smackmapbackend.smacker

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

data class SmackerResponse(
    val id: Long,
    val partnerIds: List<Long>,
    val userName: String,
    val email: String,
) {
    companion object {
        fun of(smacker: Smacker): SmackerResponse {
            return SmackerResponse(
                id = smacker.id,
                userName = smacker.userName,
                email = smacker.email,
                partnerIds = emptyList()
            )
        }
    }
}

data class GenerateSmackerLinkRequest(
    val smackerId: Long
)

data class SmackerLinkResponse(
    val smackerId: Long,
    val shareableLink: String
)

data class GetSmackerByLinkRequest(
    val smackerLink: String
)
