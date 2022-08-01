package com.lovemap.lovemapbackend.geolocation

import com.lovemap.lovemapbackend.TestUtils
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class CachedGeoLocationProviderTest {

    private val repository: GeoLocationRepository = mockk()
    private val cachedGeoLocationProvider = CachedGeoLocationProvider(repository)

    @Test
    fun testCacheInsert(): Unit = runBlocking {
        coEvery { repository.findAllCountries() } returns flow {
            emit("USA"); emit("Switzerland"); emit("France"); emit("Italy")
        }

        var result = cachedGeoLocationProvider.findAllCountries()
        TestUtils.assertContains(result.countries, "USA", "Switzerland", "France", "Italy")

        cachedGeoLocationProvider.insertIntoCache(
            GeoLocation(
                id = 1,
                postalCode = "1234",
                city = "Budapest",
                country = "Hungary",
                county = "Budapest"
            )
        )

        result = cachedGeoLocationProvider.findAllCountries()
        TestUtils.assertContains(result.countries, "USA", "Switzerland", "France", "Italy", "Hungary")
    }
}