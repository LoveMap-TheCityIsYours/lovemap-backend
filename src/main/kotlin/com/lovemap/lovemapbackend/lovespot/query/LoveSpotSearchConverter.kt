package com.lovemap.lovemapbackend.lovespot.query

import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.LoveSpotResponse
import com.lovemap.lovemapbackend.lovespot.query.ListLocationRequest.*
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import com.lovemap.lovemapbackend.utils.ValidatorService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import kotlin.math.max

const val MAX_LIMIT_LIST: Int = 1000
const val MAX_LIMIT_SEARCH: Int = 100

@Service
class LoveSpotSearchConverter(
    private val validatorService: ValidatorService
) {
    fun validateAndConvertRequest(
        listOrdering: ListOrderingRequest,
        listLocation: ListLocationRequest,
        request: LoveSpotSearchRequest
    ): LoveSpotSearchDto {
        validatorService.validate(request)
        validateOrdering(listOrdering, request)
        validateLocation(listLocation, request)
        return LoveSpotSearchDto(
            limit = max(request.limit, MAX_LIMIT_SEARCH),
            typeFilter = convertTypeFilter(request.typeFilter),
            listOrdering = listOrdering.toDto(),
            listLocation = listLocation.toDto(),
            latitude = request.latitude,
            longitude = request.longitude,
            distanceInMeters = request.distanceInMeters,
            locationName = request.locationName
        )
    }

    private fun validateOrdering(
        listOrdering: ListOrderingRequest,
        request: LoveSpotSearchRequest
    ) {
        if (listOrdering == ListOrderingRequest.CLOSEST) {
            if (request.latitude == null || request.longitude == null) {
                throw LoveMapException(
                    status = HttpStatus.BAD_REQUEST,
                    errorCode = ErrorCode.MissingListCoordinates,
                    subject = "null",
                    message = "Missing lat, long from LoveSpotSearchRequest"
                )
            }
        }
    }

    private fun validateLocation(
        listLocation: ListLocationRequest,
        request: LoveSpotSearchRequest
    ) {
        when (listLocation) {
            COUNTRY -> {
                request.locationName ?: throw LoveMapException(
                    status = HttpStatus.BAD_REQUEST,
                    errorCode = ErrorCode.MissingListCountry,
                    subject = "null",
                    message = "Missing locationName from LoveSpotSearchRequest"
                )
            }
            CITY -> {
                request.locationName ?: throw LoveMapException(
                    status = HttpStatus.BAD_REQUEST,
                    errorCode = ErrorCode.MissingListCity,
                    subject = "null",
                    message = "Missing locationName from LoveSpotSearchRequest"
                )
            }
            COORDINATE -> {
                if (request.latitude == null || request.longitude == null || request.distanceInMeters == null) {
                    throw LoveMapException(
                        status = HttpStatus.BAD_REQUEST,
                        errorCode = ErrorCode.MissingListCoordinates,
                        subject = "null",
                        message = "Missing lat, long or distanceInMeters from LoveSpotSearchRequest"
                    )
                }
            }
        }
    }

    private fun convertTypeFilter(typeFilter: List<LoveSpotResponse.Type>): Set<LoveSpot.Type> {
        return if (typeFilter.isEmpty()) {
            LoveSpot.Type.values().toSet()
        } else {
            typeFilter.map { it.toEntity() }.toSet()
        }
    }
}