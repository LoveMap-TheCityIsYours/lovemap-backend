package com.lovemap.lovemapbackend.tracking

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant

@Document(collection = "userTracks")
@CompoundIndexes(
    CompoundIndex(
        def = "{ 'lastActivityAt': -1, 'lastActivityNotificationAt': -1, 'firebaseToken': 1 }"
    )
)
data class UserTrack(
    @Id
    val objectId: ObjectId,
    @Field("id")
    @Indexed(name = "loverIdUnique", unique = true)
    val loverId: Long,
    val displayName: String,
    @Indexed
    val lastActivityAt: Instant,
    val lastActivityNotificationAt: Instant? = null,
    val firebaseToken: String? = null,
    val haveOthersBeenNotifiedAboutThisPublicUserJoining: Boolean = false,

    // tracking:
    val lastKnownLocation: LastKnownLocation? = null,
    val loveSpotSearches: ResourceAccess? = null,
    val hallOfFame: ResourceAccess? = null,
    val newsFeed: ResourceAccess? = null,
    val getFollowers: ResourceAccess? = null,
    val getFollowings: ResourceAccess? = null,
    val loverActivities: LoverActivities? = null,
) {

    data class LastKnownLocation(
        val latitude: Double,
        val longitude: Double
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
