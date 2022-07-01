package com.lovemap.lovemapbackend.lovespot.list.strategy.coordinate

import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import com.lovemap.lovemapbackend.lovespot.list.strategy.LoveSpotListStrategy
import kotlin.math.sqrt

abstract class CoordinateBasedStrategy(
    center: LatLng,
    distance: Int,
    protected val limit: Int
) : LoveSpotListStrategy {

    private val upperLeftAngle = 315.0
    private val lowerRightAngle = 135.0
    private val sqrt2 = sqrt(2.0)

    protected val from: LatLng
    protected val to: LatLng

    init {
        val travelDistance = distance * sqrt2
        val upperLeft = LatLngTool.travel(center, upperLeftAngle, travelDistance, LengthUnit.METER)
        val lowerRight = LatLngTool.travel(center, lowerRightAngle, travelDistance, LengthUnit.METER)

        from = LatLng(upperLeft.latitude, upperLeft.longitude)
        to = LatLng(lowerRight.latitude, lowerRight.longitude)
    }
}
