package com.lovemap.lovemapbackend.lovespot

import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import com.lovemap.lovemapbackend.authentication.security.AuthorizationService
import com.lovemap.lovemapbackend.geolocation.GeoLocationService
import com.lovemap.lovemapbackend.lover.ranking.LoverPointService
import com.lovemap.lovemapbackend.newsfeed.LoveSpotNewsFeedUpdater
import com.lovemap.lovemapbackend.notification.NotificationService
import com.lovemap.lovemapbackend.notification.NotificationType.NEW_LOVE_SPOT
import com.lovemap.lovemapbackend.utils.AsyncTaskService
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.ErrorMessage
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.time.Instant

private const val TWELVE_METERS_IN_COORDINATES = 0.0001
private const val MINIMUM_DISTANCE_IN_METERS = 20.0

@Service
class LoveSpotService(
    private val authorizationService: AuthorizationService,
    private val loverPointService: LoverPointService,
    private val geoLocationService: GeoLocationService,
    private val asyncTaskService: AsyncTaskService,
    private val cachedLoveSpotService: CachedLoveSpotService,
    private val loveSpotNewsFeedUpdater: LoveSpotNewsFeedUpdater,
    private val notificationService: NotificationService,
    private val repository: LoveSpotRepository
) {

    suspend fun getById(spotId: Long): LoveSpot {
        val loveSpot = (repository.findById(spotId)
            ?: throw LoveMapException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    ErrorCode.LoveSpotNotFound,
                    spotId.toString(),
                    "LoveSpot not found by ID '$spotId'."
                )
            ))

        if (loveSpot.geoLocationId == null) {
            asyncTaskService.runAsync {
                setGeoLocation(loveSpot, true)
            }
        } else {
            cachedLoveSpotService.put(loveSpot)
        }

        return loveSpot
    }

    suspend fun authorizedGetById(spotId: Long): LoveSpot {
        val loveSpot = getById(spotId)
        authorizationService.checkAccessFor(loveSpot)
        return loveSpot
    }

    suspend fun create(request: CreateLoveSpotRequest): LoveSpot {
        val caller = authorizationService.getCaller()
        val loveSpot = LoveSpot(
            name = request.name.trim(),
            longitude = request.longitude,
            latitude = request.latitude,
            addedBy = caller.id,
            description = request.description.trim(),
            availability = request.availability.toEntity(),
            type = request.type.toEntity(),
            createdAt = Timestamp.from(Instant.now())
        )
        loveSpot.setCustomAvailability(request.customAvailability)
        validateSpotTooClose(request)
        val savedSpot = repository.save(loveSpot)
        asyncTaskService.runAsync {
            loverPointService.addPointsForSpotAdded(savedSpot)
            setGeoLocation(loveSpot)
        }
        notificationService.sendNearbyLoveSpotNotification(savedSpot, NEW_LOVE_SPOT)
        return savedSpot
    }

    private suspend fun validateSpotTooClose(request: CreateLoveSpotRequest) {
        if (anySpotsTooClose(request)) {
            throw LoveMapException(
                HttpStatus.BAD_REQUEST,
                ErrorMessage(
                    ErrorCode.SpotTooCloseToAnother,
                    request.name,
                    "LoveSpot too close to another, cannot be added here. " +
                            "Minimum distance is > ${MINIMUM_DISTANCE_IN_METERS}m."
                )
            )
        }
    }

    private suspend fun setGeoLocation(loveSpot: LoveSpot, updateNewsFeed: Boolean = false) {
        val geoLocation = geoLocationService.decodeLocationInfo(loveSpot)
        geoLocation?.let {
            loveSpot.geoLocationId = geoLocation.id
            repository.save(loveSpot)
            cachedLoveSpotService.put(loveSpot)
            if (updateNewsFeed) {
                geoLocation.country?.let {
                    loveSpotNewsFeedUpdater.updateLoveSpotCountry(loveSpot.id, it)
                }
            }
        }
    }

    private suspend fun anySpotsTooClose(request: CreateLoveSpotRequest): Boolean {
        val nearbySpots = repository.findByCoordinatesOrderByRating(
            latFrom = request.latitude - TWELVE_METERS_IN_COORDINATES,
            longFrom = request.longitude - TWELVE_METERS_IN_COORDINATES,
            latTo = request.latitude + TWELVE_METERS_IN_COORDINATES,
            longTo = request.longitude + TWELVE_METERS_IN_COORDINATES,
            typeFilter = LoveSpot.Type.values().toSet(),
            limit = 100
        )

        return nearbySpots.toList().any { nearby ->
            LatLngTool.distance(
                LatLng(nearby.latitude, nearby.longitude),
                LatLng(request.latitude, request.longitude),
                LengthUnit.METER
            ) <= MINIMUM_DISTANCE_IN_METERS
        }
    }

    suspend fun update(id: Long, request: UpdateLoveSpotRequest): LoveSpot {
        val loveSpot = getById(id)
        authorizationService.checkAccessFor(loveSpot)
        request.name?.let { loveSpot.name = it.trim() }
        request.description?.let { loveSpot.description = it.trim() }
        request.availability?.let { loveSpot.availability = it.toEntity() }
        request.type?.let { loveSpot.type = it.toEntity() }
        loveSpot.setCustomAvailability(request.customAvailability)
        return repository.save(loveSpot)
    }

    suspend fun checkExistence(loveSpotId: Long) {
        if (!repository.existsById(loveSpotId)) {
            throw LoveMapException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    ErrorCode.LoveSpotNotFound,
                    loveSpotId.toString(),
                    "LoveSpot '$loveSpotId' does not exist."
                )
            )
        }
    }

    fun findAllByIds(locationIds: Flow<Long>): Flow<LoveSpot> {
        return repository.findAllById(locationIds)
    }

    fun findAllByIds(locationIds: Iterable<Long>): Flow<LoveSpot> {
        return repository.findAllById(locationIds)
    }

    suspend fun deleteLoveSpot(loveSpot: LoveSpot) {
        loverPointService.subtractPointsForSpotDeleted(loveSpot)
        repository.delete(loveSpot)
    }

    suspend fun save(loveSpot: LoveSpot): LoveSpot {
        return repository.save(loveSpot)
    }

    fun getLoveSpotsFrom(generateFrom: Instant): Flow<LoveSpot> {
        return repository.findAllAfterCreatedAt(Timestamp.from(generateFrom))
    }
}
