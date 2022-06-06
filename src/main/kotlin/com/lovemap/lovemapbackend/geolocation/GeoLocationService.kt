package com.lovemap.lovemapbackend.geolocation

import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.model.AddressComponent
import com.google.maps.model.LatLng
import com.lovemap.lovemapbackend.lovespot.LoveSpot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct

private const val UNKNOWN_GEO_LOCATION: Long = 1

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
class GeoLocationService(
    private val geoApiContext: GeoApiContext,

    private val repository: GeoLocationRepository,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun getLocationInfo(loveSpot: LoveSpot): GeoLocation? {
        return withContext(Dispatchers.IO) {
            logger.info { "Reverse geocoding $loveSpot" }
            val geoLocation = decodeLocation(loveSpot)
            saveOrGetExisting(geoLocation)
        }
    }

    suspend fun saveOrGetExisting(geoLocation: GeoLocation): GeoLocation? {
        if (geoLocation.isUnknown()) {
            return repository.findById(UNKNOWN_GEO_LOCATION)
        }
        val savedLocation = repository.findByPostalCodeAndCityAndCountyAndCountry(
            geoLocation.postalCode,
            geoLocation.city,
            geoLocation.county,
            geoLocation.country
        )
        return savedLocation ?: repository.save(geoLocation)
    }

    fun decodeLocation(loveSpot: LoveSpot): GeoLocation {
        val geoResult = GeoLocation()
        val geocodingResults = GeocodingApi
            .reverseGeocode(geoApiContext, LatLng(loveSpot.latitude, loveSpot.longitude))
            .await()
            .toList()
        geocodingResults.flatMap { it.addressComponents.toList() }
            .forEach { ac ->
                setGeoResult(geoResult, ac)
            }
        return geoResult
    }

    private fun setGeoResult(
        geoResult: GeoLocation,
        ac: AddressComponent
    ) {
        if (geoResult.postalCode == null && isPostalCode(ac)) {
            geoResult.postalCode = ac.longName
        } else if (geoResult.city == null && isCity(ac)) {
            geoResult.city = ac.longName
        } else if (geoResult.county == null && isCounty(ac)) {
            geoResult.county = ac.longName
        } else if (geoResult.country == null && isCountry(ac)) {
            geoResult.country = ac.longName
        }
    }

    private fun isPostalCode(ac: AddressComponent) =
        ac.types.toList().any { type -> "POSTAL_CODE" == type.name }

    private fun isCity(ac: AddressComponent) =
        ac.types.toList().any { type -> "LOCALITY" == type.name }

    private fun isCounty(ac: AddressComponent) =
        ac.types.toList().any { type -> "ADMINISTRATIVE_AREA_LEVEL_1" == type.name }

    private fun isCountry(ac: AddressComponent) =
        ac.types.toList().any { type -> "COUNTRY" == type.name }

    @PostConstruct
    fun testShit() {
//        mono {
//            val loveSpotFlow = loveSpotService.search(LoveSpotSearchRequest(-100.0, -100.0, 100.0, 100.0, 200))
//            loveSpotFlow.collect {
//                getLocationInfo(it)
//            }
//        }.subscribe()
    }
}