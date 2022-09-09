package com.lovemap.lovemapbackend.lovespot.query.strategy.coordinate

import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import com.lovemap.lovemapbackend.lovespot.query.LoveSpotSearchDto
import com.lovemap.lovemapbackend.lovespot.query.LoveSpotDistanceSorter
import com.lovemap.lovemapbackend.lovespot.query.strategy.LoveSpotSearchStrategy
import kotlin.math.sqrt

abstract class CoordinateBasedStrategy(
    protected val sorter: LoveSpotDistanceSorter
) : LoveSpotSearchStrategy {

    private val upperLeftAngle = 315.0
    private val lowerRightAngle = 135.0
    private val sqrt2 = sqrt(2.0)

    final override suspend fun listSpots(listDto: LoveSpotSearchDto): List<LoveSpot> {
        val distance = listDto.distanceInMeters!!
        val center = LatLng(listDto.latitude!!, listDto.longitude!!)

        val travelDistance = distance * sqrt2
        val upperLeft = LatLngTool.travel(center, upperLeftAngle, travelDistance, LengthUnit.METER)
        val lowerRight = LatLngTool.travel(center, lowerRightAngle, travelDistance, LengthUnit.METER)

        val from = LatLng(upperLeft.latitude, upperLeft.longitude)
        val to = LatLng(lowerRight.latitude, lowerRight.longitude)

        return doListSpots(center, from, to, listDto.limit, listDto.typeFilter)
            .let { sorter.filterByDistance(center, listDto.distanceInMeters, it) }
    }

    abstract suspend fun doListSpots(
        center: LatLng,
        from: LatLng,
        to: LatLng,
        limit: Int,
        typeFilter: Set<LoveSpot.Type>
    ): List<LoveSpot>
}
