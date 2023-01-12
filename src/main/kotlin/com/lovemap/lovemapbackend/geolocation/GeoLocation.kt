package com.lovemap.lovemapbackend.geolocation

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column

data class GeoLocation(
    @Id
    var id: Long = 0,

    @Column("postal_code")
    var postalCode: String? = null,

    @Column("city")
    var city: String? = null,

    @Column("county")
    var county: String? = null,

    @Column("country")
    var country: String? = null,
) {
    fun isUnknown() =
        postalCode == null && city == null && county == null && country == null

    companion object {
        const val GLOBAL_LOCATION = "GLOBAL"
    }
}
