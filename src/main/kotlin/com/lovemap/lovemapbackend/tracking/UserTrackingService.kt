package com.lovemap.lovemapbackend.tracking

import com.lovemap.lovemapbackend.lover.Lover
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserTrackingService(
    private val mongoTemplate: ReactiveMongoTemplate,
) {

    suspend fun trackUser(caller: Lover, requestPath: String) {
        runCatching {
            var updated = false
            val now = Instant.now()
            val query = Query(Criteria.where("id").`is`(caller.id))
            val update = Update()
                .set("lastActivityAt", now)
                .set("displayName", caller.displayName)

            if (requestPath.contains("hallOfFame")) {
                update.inc("hallOfFame.accessCount")
                update.set("hallOfFame.lastAccessedAt", now)
                updated = true

            } else if (requestPath.contains("lovespots/search")) {
                update.inc("loveSpotSearches.accessCount")
                update.set("loveSpotSearches.lastAccessedAt", now)
                updated = true

            } else if (requestPath.contains("newsfeed")) {
                update.inc("newsFeed.accessCount")
                update.set("newsFeed.lastAccessedAt", now)
                updated = true

            } else if (requestPath.contains("relations/followers")) {
                update.inc("getFollowers.accessCount")
                update.set("getFollowers.lastAccessedAt", now)
                updated = true

            } else if (requestPath.contains("relations/followings")) {
                update.inc("getFollowings.accessCount")
                update.set("getFollowings.lastAccessedAt", now)
                updated = true

            } else if (requestPath.contains("/activities")) {
                runCatching {
                    requestPath.substringBefore("/activities").substringAfter("lovers/").toLong()
                }.onSuccess { loverId ->
                    update.inc("loverActivities.idAccessCounts.$loverId")
                }
                update.inc("loverActivities.accessCount")
                update.set("loverActivities.lastAccessedAt", now)
                updated = true
            }

            if (updated) {
                mongoTemplate.upsert(query, update, UserTrack::class.java).subscribe()
            }
        }
    }

    suspend fun trackLocation(caller: Lover, latitude: Double?, longitude: Double?) {
        runCatching {
            val now = Instant.now()
            val query = Query(Criteria.where("id").`is`(caller.id))
            val update = Update()
                .set("lastActivityAt", now)
                .set("displayName", caller.displayName)
            if (latitude != null && longitude != null) {
                update.set("lastKnownLocation.latitude", latitude)
                update.set("lastKnownLocation.longitude", longitude)
            }
            mongoTemplate.upsert(query, update, UserTrack::class.java).subscribe()
        }
    }

}
