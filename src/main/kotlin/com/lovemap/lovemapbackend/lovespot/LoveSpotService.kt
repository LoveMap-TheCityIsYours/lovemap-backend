package com.lovemap.lovemapbackend.lovespot

import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import com.lovemap.lovemapbackend.geolocation.GeoLocationService
import com.lovemap.lovemapbackend.lover.LoverPointService
import com.lovemap.lovemapbackend.lovespot.report.LoveSpotReportRequest
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReview
import com.lovemap.lovemapbackend.lovespot.review.LoveSpotReviewRequest
import com.lovemap.lovemapbackend.security.AuthorizationService
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.ErrorMessage
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private const val TWELVE_METERS_IN_COORDINATES = 0.0001
private const val MINIMUM_DISTANCE_IN_METERS = 20.0

@Service
@Transactional
class LoveSpotService(
    private val authorizationService: AuthorizationService,
    private val loverPointService: LoverPointService,
    private val geoLocationService: GeoLocationService,
    private val repository: LoveSpotRepository
) {
    private val logger = KotlinLogging.logger {}

    suspend fun getById(spotId: Long): LoveSpot {
        val loveSpot = (repository.findById(spotId)
            ?: throw LoveMapException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    ErrorCode.NotFoundById,
                    spotId.toString(),
                    "LoveSpot not found by ID '$spotId'."
                )
            ))

        if (loveSpot.geoLocationId == null) {
            CoroutineScope(Dispatchers.Default).async {
                setGeoLocation(loveSpot)
            }
        }

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
        )
        loveSpot.setCustomAvailability(request.customAvailability)
        validateSpotTooClose(request)
        val savedSpot = repository.save(loveSpot)
        runAsyncTasks(savedSpot, loveSpot)
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

    private suspend fun runAsyncTasks(
        savedSpot: LoveSpot,
        loveSpot: LoveSpot
    ) {
        CoroutineScope(Dispatchers.Default).async {
            loverPointService.addPointsForSpotAdded(savedSpot)
            setGeoLocation(loveSpot)
        }
    }

    private suspend fun setGeoLocation(loveSpot: LoveSpot) {
        val geoLocation = geoLocationService.getLocationInfo(loveSpot)
        geoLocation?.let {
            loveSpot.geoLocationId = geoLocation.id
            repository.save(loveSpot)
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

    suspend fun updateReviewAverages(spotId: Long, request: LoveSpotReviewRequest): LoveSpot {
        val loveSpot = getById(spotId)
        if (loveSpot.averageRating == null) {
            loveSpot.averageRating = request.reviewStars.toDouble()
            loveSpot.averageDanger = request.riskLevel.toDouble()
            loveSpot.numberOfRatings = 1
        } else {
            var averageRatingWeight = loveSpot.averageRating!! * loveSpot.numberOfRatings
            var averageDangerWeight = loveSpot.averageDanger!! * loveSpot.numberOfRatings
            loveSpot.numberOfRatings++

            averageRatingWeight += request.reviewStars
            loveSpot.averageRating = averageRatingWeight / loveSpot.numberOfRatings

            averageDangerWeight += request.riskLevel
            loveSpot.averageDanger = averageDangerWeight / loveSpot.numberOfRatings
        }
        return repository.save(loveSpot)
    }

    suspend fun reviseReviewAverages(previousReview: LoveSpotReview, request: LoveSpotReviewRequest): LoveSpot {
        val loveSpot = getById(previousReview.loveSpotId)
        var averageRatingWeight = loveSpot.averageRating!! * loveSpot.numberOfRatings
        var averageDangerWeight = loveSpot.averageDanger!! * loveSpot.numberOfRatings
        averageRatingWeight = averageRatingWeight - previousReview.reviewStars + request.reviewStars
        averageDangerWeight = averageDangerWeight - previousReview.riskLevel + request.riskLevel
        loveSpot.averageRating = averageRatingWeight / loveSpot.numberOfRatings
        loveSpot.averageDanger = averageDangerWeight / loveSpot.numberOfRatings
        return repository.save(loveSpot)
    }

    suspend fun checkExistence(loveSpotId: Long) {
        if (!repository.existsById(loveSpotId)) {
            throw LoveMapException(
                HttpStatus.NOT_FOUND,
                ErrorMessage(
                    ErrorCode.NotFoundById,
                    loveSpotId.toString(),
                    "LoveSpot '$loveSpotId' does not exist."
                )
            )
        }
    }

    fun findAllByIds(locationIds: Flow<Long>): Flow<LoveSpot> {
        return repository.findAllById(locationIds)
    }

    fun findAllByIds(locationIds: List<Long>): Flow<LoveSpot> {
        return repository.findAllById(locationIds)
    }

    suspend fun updateNumberOfReports(loveSpotId: Long, request: LoveSpotReportRequest): LoveSpot {
        val loveSpot = getById(loveSpotId)
        loveSpot.numberOfReports += 1
        return repository.save(loveSpot)
    }

    suspend fun deleteLoveSpot(loveSpot: LoveSpot) {
        loverPointService.subtractPointsForSpotDeleted(loveSpot)
        repository.delete(loveSpot)
    }
}