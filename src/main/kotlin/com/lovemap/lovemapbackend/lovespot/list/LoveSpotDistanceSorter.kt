package com.lovemap.lovemapbackend.lovespot.list

import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit.METER
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import org.springframework.stereotype.Component

@Component
class LoveSpotDistanceSorter {

    fun sortAndFilter(center: LatLng, distanceLimitMeters: Int, inputList: List<LoveSpot>): List<LoveSpot> {
        return inputList.let { filterByDistance(center, distanceLimitMeters, it) }
            .let { sortList(center, it) }
    }

    fun sortList(center: LatLng, inputList: List<LoveSpot>): List<LoveSpot> {
        return inputList.sortedWith(LoveSpotDistanceComparator(center))
    }

    fun filterByDistance(center: LatLng, distanceLimitMeters: Int, inputList: List<LoveSpot>): List<LoveSpot> {
        return inputList
            .filter { LatLngTool.distance(center, LatLng(it.latitude, it.longitude), METER) <= distanceLimitMeters }
    }

    class LoveSpotDistanceComparator(private val center: LatLng) : Comparator<LoveSpot> {
        override fun compare(o1: LoveSpot, o2: LoveSpot): Int {
            val distance1 = LatLngTool.distance(center, LatLng(o1.latitude, o1.longitude), METER).toInt()
            val distance2 = LatLngTool.distance(center, LatLng(o2.latitude, o2.longitude), METER).toInt()
            return distance1 - distance2
        }
    }
}