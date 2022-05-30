package com.lovemap.lovemapbackend

import com.javadocmd.simplelatlng.LatLng
import com.javadocmd.simplelatlng.LatLngTool
import com.javadocmd.simplelatlng.util.LengthUnit
import org.junit.jupiter.api.Test

class LatLngTest {

    @Test
    fun testDistance() {
        for (i in 1..10) {
            val random = LatLng.random()
            val other = LatLng(random.latitude + 0.00005, random.longitude + 0.00005)
            val distance = LatLngTool.distance(random, other, LengthUnit.METER)

            println(distance)
        }
    }

}
