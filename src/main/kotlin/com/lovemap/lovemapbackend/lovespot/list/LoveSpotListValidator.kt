package com.lovemap.lovemapbackend.lovespot.list

import com.lovemap.lovemapbackend.lovespot.LoveSpotAdvancedListRequest
import com.lovemap.lovemapbackend.lovespot.ListLocation
import com.lovemap.lovemapbackend.lovespot.ListLocation.*
import com.lovemap.lovemapbackend.lovespot.ListOrdering
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import com.lovemap.lovemapbackend.utils.ValidatorService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class LoveSpotListValidator(
    private val validatorService: ValidatorService
) {

    fun validateRequest(
        listOrdering: ListOrdering,
        listLocation: ListLocation,
        request: LoveSpotAdvancedListRequest
    ) {
        validatorService.validate(request)
        if (listOrdering == ListOrdering.CLOSEST) {
            if (request.lat == null || request.long == null) {
                throw LoveMapException(
                    status = HttpStatus.BAD_REQUEST,
                    errorCode = ErrorCode.MissingListCoordinates,
                    subject = "null",
                    message = "Missing lat, long from LoveSpotSearchRequest"
                )
            }
        }
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
                if (request.lat == null || request.long == null || request.distanceInMeters == null) {
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
}