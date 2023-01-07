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
        coEvery { repository.findAllDistinctCountries() } returns flow {
            emit(GeoLocation(1, country = "USA"))
            emit(GeoLocation(2, country = "Switzerland"))
            emit(GeoLocation(3, country = "France"))
            emit(GeoLocation(4, country = "Italy"))
        }

        var result = cachedGeoLocationProvider.findAllCountries()
        TestUtils.assertContains(result.countries, "USA", "Switzerland", "France", "Italy")

        cachedGeoLocationProvider.insertIntoCache(
            GeoLocation(
                id = 5,
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