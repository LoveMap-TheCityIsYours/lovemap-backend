package com.lovemap.lovemapbackend.lover.partnership

data class LoverPartnershipV2Response(
    val loverId: Long,
    val partnership: PartnershipResponse?
) {
    companion object {
        fun of(loverPartnerShips: LoverPartnership): LoverPartnershipV2Response {
            return LoverPartnershipV2Response(
                loverId = loverPartnerShips.loverId,
                partnership = loverPartnerShips.partnership?.let { PartnershipResponse.of(it) }
            )
        }
    }
}
