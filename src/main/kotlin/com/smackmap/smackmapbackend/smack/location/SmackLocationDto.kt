package com.smackmap.smackmapbackend.smack.location

data class SmackLocationDto(
    val id: Long,
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val averageRating: Double? = null,
) {
    companion object {
        fun of(smackLocation: SmackLocation): SmackLocationDto {
            return SmackLocationDto(
                id = smackLocation.id,
                name = smackLocation.name,
                longitude = smackLocation.longitude,
                latitude = smackLocation.latitude,
                averageRating = smackLocation.averageRating
            )
        }
    }
}

data class CreateSmackLocationRequest(
    val name: String,
    val longitude: Double,
    val latitude: Double,
)

data class SmackLocationSearchRequest(
    val latFrom: Double,
    val longFrom: Double,
    val latTo: Double,
    val longTo: Double,
    val limit: Int
)
