package com.lovemap.lovemapbackend.geolocation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class CachedGeoLocationProvider(
    private val repository: GeoLocationRepository
) {
    private val cityCache = ConcurrentHashMap<City, Unit>()
    private val countryCache = ConcurrentHashMap<String, Unit>()

    fun insertIntoCache(geoLocation: GeoLocation) {
        if (geoLocation.country?.isEmpty() == false) {
            if (!countryCache.contains(geoLocation.country)) {
                countryCache[geoLocation.country!!] = Unit
            }
            if (geoLocation.city?.isEmpty() == false) {
                val city = City(geoLocation.country!!, geoLocation.city!!)
                if (!cityCache.contains(city)) {
                    cityCache[city] = Unit
                }
            }
        }
    }

    suspend fun findAllCountries(): Countries {
        return if (countryCache.isNotEmpty()) {
            Countries(countryCache.keys().toList())
        } else {
            withContext(Dispatchers.IO) {
                val countries = repository.findAllCountries().toList()
                synchronized(countryCache) {
                    if (countryCache.isEmpty()) {
                        countryCache.putAll(countries.map { Pair(it, Unit) })
                    }
                }
                Countries(countries)
            }
        }
    }

    suspend fun findAllCities(): Cities {
        return if (cityCache.isNotEmpty()) {
            Cities(cityCache.keys().toList())
        } else {
            withContext(Dispatchers.IO) {
                val cities = repository.findAllCities().map { City(it.country!!, it.city!!) }.toList()
                synchronized(cityCache) {
                    if (cityCache.isEmpty()) {
                        cityCache.putAll(cities.map { Pair(it, Unit) })
                    }
                }
                Cities(cities)
            }
        }
    }
}