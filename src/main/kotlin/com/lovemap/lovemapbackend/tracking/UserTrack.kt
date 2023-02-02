package com.lovemap.lovemapbackend.tracking

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "userTracks")
data class UserTrack(

    @Id
    @Indexed
    val loverId: Long,

    val displayName: String,

    @Indexed
    val lastActivityAt: Instant,

    val lastKnownLocation: LastKnownLocation? = null,

    val accessedResources: AccessedResources

) {

    data class LastKnownLocation(
        val latitude: Double,
        val longitude: Double
    )

    data class AccessedResources(
        val loveSpotSearches: ResourceAccess? = null,
        val hallOfFame: ResourceAccess? = null,
        val newsFeed: ResourceAccess? = null,
        val getFollowers: ResourceAccess? = null,
        val getFollowings: ResourceAccess? = null,
        val loverActivities: LoverActivities? = null,
    )

    data class ResourceAccess(
        val lastAccessedAt: Instant,
        val accessCount: Long
    )

    data class LoverActivities(
        val lastAccessedAt: Instant,
        val accessCount: Long,
        val idAccessCounts: Map<Long, Long>
    )
}
