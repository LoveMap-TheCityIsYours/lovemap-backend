package com.lovemap.lovemapbackend.lovespot

import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import com.lovemap.lovemapbackend.love.LoveService
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import kotlin.math.sqrt

@Service
class LoveSpotSearchService(
    private val loveSpotService: LoveSpotService,
    private val loveService: LoveService,
    private val loveSpotRepository: LoveSpotRepository,
) {
    private val upperLeftAngle = 315.0
    private val lowerRightAngle = 135.0
    private val sqrt2 = sqrt(2.0)

    suspend fun search(
        searchType: SearchType,
        searchLocation: SearchLocation,
        request: LoveSpotSearchRequest
    ): List<LoveSpotDto> {
        return when (searchLocation) {
            SearchLocation.COORDINATE -> {
                findByCoordinate(searchType, request)
            }
            SearchLocation.CITY -> {
                findByCity(searchType, request)
            }
            SearchLocation.COUNTRY -> {
                findByCountry(searchType, request)
            }
        }
    }

    private suspend fun findByCoordinate(searchType: SearchType, request: LoveSpotSearchRequest): List<LoveSpotDto> {
        val middlePoint = LatLng(request.lat!!, request.long!!)
        val distance = request.distance!! * sqrt2
        val upperLeft = LatLngTool.travel(middlePoint, upperLeftAngle, distance, LengthUnit.METER)
        val lowerRight = LatLngTool.travel(middlePoint, lowerRightAngle, distance, LengthUnit.METER)
        return when (searchType) {
            SearchType.BEST -> {
                loveSpotRepository.searchWithOrderByBest(
                    latFrom = upperLeft.latitude,
                    longFrom = upperLeft.longitude,
                    latTo = lowerRight.latitude,
                    longTo = lowerRight.longitude,
                    limit = request.limit
                ).map { LoveSpotDto.of(it) }.toList()
            }
            SearchType.CLOSEST -> {
                loveSpotRepository.searchWithOrderByClosest(
                    latFrom = upperLeft.latitude,
                    longFrom = upperLeft.longitude,
                    latTo = lowerRight.latitude,
                    longTo = lowerRight.longitude,
                    centerLat = request.lat,
                    centerLong = request.long,
                    limit = request.limit
                ).map { LoveSpotDto.of(it) }.toList()
            }
            SearchType.HOT -> {
                // TODO: implement comments
                loveSpotRepository.searchWithOrderByLastMadeLove(
                    latFrom = upperLeft.latitude,
                    longFrom = upperLeft.longitude,
                    latTo = lowerRight.latitude,
                    longTo = lowerRight.longitude,
                    limit = request.limit
                ).map { LoveSpotDto.of(it) }.toList()
            }
        }
    }

    private suspend fun findByCountry(searchType: SearchType, request: LoveSpotSearchRequest): List<LoveSpotDto> {
        TODO("Not yet implemented")
    }

    private suspend fun findByCity(searchType: SearchType, request: LoveSpotSearchRequest): List<LoveSpotDto> {
        TODO("Not yet implemented")
    }
}