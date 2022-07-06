package com.lovemap.lovemapbackend.geolocation

data class Countries(
    val countries: List<String>
)

data class Cities(
    val cities: List<City>
)

data class City(
    val country: String,
    val city: String
)