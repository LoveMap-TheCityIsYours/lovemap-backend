package com.lovemap.lovemapbackend.tracking

import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import com.lovemap.lovemapbackend.lover.Lover
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

@Service
class UserTrackingService(
    private val repository: UserTrackRepository,
    private val mongoTemplate: ReactiveMongoTemplate,
) {
    private val upperLeftAngle = 315.0
    private val lowerRightAngle = 135.0

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

    suspend fun saveFirebaseToken(loverId: Long, token: String) {
        runCatching {
            val query = Query(Criteria.where("id").`is`(loverId))
            val update = Update().set("firebaseToken", token)
            mongoTemplate.upsert(query, update, UserTrack::class.java).subscribe()
        }

    }

    fun updateActivityNotification(loverId: Long, instant: Instant) {
        runCatching {
            val query = Query(Criteria.where("id").`is`(loverId))
            val update = Update().set("lastActivityNotificationAt", instant)
            mongoTemplate.upsert(query, update, UserTrack::class.java).subscribe()
        }
    }

    fun updateActivityNotifications(loverIds: Collection<Long>, instant: Instant) {
        runCatching {
            val query = Query(Criteria.where("id").`in`(loverIds))
            val update = Update().set("lastActivityNotificationAt", instant)
            mongoTemplate.updateMulti(query, update, UserTrack::class.java).subscribe()
        }
    }

    fun findUnnotifiedInactivesBefore(instant: Instant): Flow<UserTrack> {
        return repository.findByLastActivityAndNotificationBefore(instant).asFlow()
    }


    fun findUsersWithinMetersOf(latitude: Double, longitude: Double, meters: Long): Flow<UserTrack> {
        val travelDistance = meters * sqrt(2.0)
        val center = LatLng(latitude, longitude)
        val upperLeft = LatLngTool.travel(center, upperLeftAngle, travelDistance, LengthUnit.METER)
        val lowerRight = LatLngTool.travel(center, lowerRightAngle, travelDistance, LengthUnit.METER)
        val latFrom = min(upperLeft.latitude, lowerRight.latitude)
        val latTo = max(upperLeft.latitude, lowerRight.latitude)
        val longFrom = min(upperLeft.longitude, lowerRight.longitude)
        val longTo = max(upperLeft.longitude, lowerRight.longitude)
        return repository.findUsersInAreaOf(latFrom, longFrom, latTo, longTo).asFlow()
    }
}
